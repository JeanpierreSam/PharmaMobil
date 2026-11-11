package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pe.edu.upeu.pharmamobile.domain.model.EstadoEnvio
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResumenSync
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/**
 * Doble del repositorio para las pruebas del ViewModel: sin latencia real, con
 * la misma baja lógica que PharmaSoft y capaz de fallar a voluntad.
 *
 * Sesión 11: igual que la base real, la «tabla» es un Flow.
 * Sesión 13: también el estado de envío; [resumen] es lo que devuelve sincronizar().
 */
class FakeProductoRepository(
    productos: List<Producto> = emptyList(),
    var retardoMs: Long = 0L
) : ProductoRepository {

    private val tabla = MutableStateFlow(productos)
    private val ultimaSincronizacion = MutableStateFlow<Long?>(null)
    private val estadoEnvio = MutableStateFlow(EstadoEnvio())

    /** Lo que hay ahora en la «base». */
    val productos: List<Producto> get() = tabla.value

    var envio: EstadoEnvio
        get() = estadoEnvio.value
        set(valor) { estadoEnvio.value = valor }

    var resumen = ResumenSync(enviadas = 0, fallidas = 0, pendientes = 0)

    var fallaAlSincronizar: Throwable? = null
    var fallaAlObtener: Throwable? = null
    var fallaAlRegistrar: Throwable? = null
    var fallaAlActualizar: Throwable? = null
    var fallaAlEliminar: Throwable? = null

    var llamadasASincronizar = 0
        private set
    var reintentosPedidos = 0
        private set

    override fun observar(): Flow<List<Producto>> = tabla

    override fun observarUltimaSincronizacion(): Flow<Long?> = ultimaSincronizacion

    override fun observarEstadoEnvio(): Flow<EstadoEnvio> = estadoEnvio

    override suspend fun sincronizar(): ResumenSync {
        llamadasASincronizar++
        esperarOFallar(fallaAlSincronizar)
        ultimaSincronizacion.value = INSTANTE_SINCRONIZACION
        return resumen
    }

    override suspend fun reintentarFallidos() {
        reintentosPedidos++
        envio = EstadoEnvio(pendientes = envio.pendientes + envio.fallidas.size)
    }

    override suspend fun descartarPendiente(id: Long) {
        tabla.value = tabla.value.filterNot { it.id == id }
        envio = envio.copy(fallidas = envio.fallidas.filterNot { it.id == id })
    }

    var borradosLocales = 0
        private set

    override suspend fun cambiosSinEnviar(): Int = envio.pendientes + envio.fallidas.size

    override suspend fun borrarDatosLocales() {
        borradosLocales++
        tabla.value = emptyList()
        envio = EstadoEnvio()
    }

    override suspend fun obtener(id: Long): Producto {
        esperarOFallar(fallaAlObtener)
        return tabla.value.first { it.id == id }
    }

    override suspend fun registrar(producto: Producto): Producto {
        esperarOFallar(fallaAlRegistrar)
        val guardado = producto.copy(id = (tabla.value.maxOfOrNull { it.id } ?: 0L) + 1L)
        tabla.value = tabla.value + guardado
        return guardado
    }

    override suspend fun actualizar(producto: Producto): Producto {
        esperarOFallar(fallaAlActualizar)
        tabla.value = tabla.value.map { if (it.id == producto.id) producto else it }
        return producto
    }

    override suspend fun eliminar(id: Long) {
        esperarOFallar(fallaAlEliminar)
        tabla.value = tabla.value.map { if (it.id == id) it.copy(activo = false) else it }
    }

    private suspend fun esperarOFallar(falla: Throwable?) {
        if (retardoMs > 0) delay(retardoMs)
        falla?.let { throw it }
    }

    companion object {
        const val INSTANTE_SINCRONIZACION = 1_000L
    }
}
