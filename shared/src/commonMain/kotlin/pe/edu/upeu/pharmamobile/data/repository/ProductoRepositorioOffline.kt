package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.platform.ahoraEnMilis

/**
 * Offline-first: la base local es la fuente de verdad y [remoto] (PharmaSoft)
 * solo la mantiene al día. Se reutiliza ProductoRepositorioRest como remoto
 * porque ya traduce los fallos de Ktor a ErrorApi y cierra la sesión ante un 401.
 */
class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoRepositorioRest,
    /** Se inyecta para que las pruebas usen una hora fija. */
    private val reloj: () -> Long = ::ahoraEnMilis
) : ProductoRepository {

    override fun observar(): Flow<List<Producto>> = local.observar()

    override fun observarUltimaSincronizacion(): Flow<Long?> = local.observarUltimaSincronizacion()

    override suspend fun sincronizar() {
        subirPendientes()                                   // 1.º lo que el usuario hizo sin red
        local.reemplazarSincronizados(remoto.listar())      // 2.º la verdad del servidor
        local.registrarSincronizacion(reloj())              // solo si las dos anteriores funcionaron
    }

    /** El formulario de edición y el detalle leen de la base: funcionan sin red. */
    override suspend fun obtener(id: Long): Producto =
        local.obtener(id) ?: throw ErrorApiException(ErrorApi.NoEncontrado)

    override suspend fun registrar(producto: Producto): Producto {
        val pendiente = local.guardarPendiente(producto)    // la pantalla ya lo ve
        return try {
            enviar(pendiente)
        } catch (e: ErrorApiException) {
            if (!e.error.dejaPendiente()) {
                // Con red, PharmaSoft lo rechazó (400, 409, 403): no se guarda y el
                // formulario muestra el error bajo su campo, como en la sesión 8.
                local.borrar(pendiente.id)
                throw e
            }
            pendiente                                       // sin red: se enviará al sincronizar
        }
    }

    override suspend fun actualizar(producto: Producto): Producto {
        if (producto.id < 0) {                              // aún no existe en PharmaSoft
            local.actualizarPendiente(producto)
            return producto
        }
        return remoto.actualizar(producto).also { local.guardarSincronizado(it) }
    }

    override suspend fun eliminar(id: Long) {
        if (id < 0) {                                       // nunca llegó al servidor: basta con quitarlo
            local.borrar(id)
            return
        }
        remoto.eliminar(id)                                 // baja lógica en PharmaSoft
        local.obtener(id)?.let { local.guardarSincronizado(it.copy(activo = false)) }
    }

    private suspend fun subirPendientes() {
        local.pendientes().forEach { pendiente ->
            try {
                enviar(pendiente)
            } catch (e: ErrorApiException) {
                // Sin red a mitad de camino: se corta y lo que falta se reintenta luego.
                if (e.error.dejaPendiente()) throw e
                // 400 o 409: PharmaSoft no lo acepta. Queda pendiente para que el
                // usuario lo corrija; no debe bloquear al resto ni a la bajada.
            }
        }
    }

    /** POST y, si responde, la fila local (id negativo) pasa a ser la del servidor. */
    private suspend fun enviar(pendiente: Producto): Producto =
        remoto.registrar(pendiente).also { local.confirmarSubida(pendiente.id, it) }

    /** Fallos de conectividad o de sesión: el dato es válido, solo hay que esperar. */
    private fun ErrorApi.dejaPendiente(): Boolean = when (this) {
        ErrorApi.SinConexion, ErrorApi.TiempoAgotado, ErrorApi.Servidor, is ErrorApi.NoAutenticado -> true
        is ErrorApi.Validacion, is ErrorApi.SinPermiso, ErrorApi.NoEncontrado, is ErrorApi.Conflicto -> false
    }
}
