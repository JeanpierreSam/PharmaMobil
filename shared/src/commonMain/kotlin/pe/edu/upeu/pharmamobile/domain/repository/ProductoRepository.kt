package pe.edu.upeu.pharmamobile.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.domain.model.EstadoEnvio
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResumenSync

/**
 * Sesión 11: el repositorio deja de devolver una foto (listar) y ofrece una
 * suscripción. La pantalla observa la base local; la red solo la mantiene al día.
 * Sesión 13: la cola de envío tiene límite y memoria, y se puede observar.
 */
interface ProductoRepository {

    /** La pantalla se suscribe una vez y se actualiza sola. */
    fun observar(): Flow<List<Producto>>

    /** Instante (epoch ms) de la última sincronización correcta; null si nunca hubo una. */
    fun observarUltimaSincronizacion(): Flow<Long?>

    /** Pendientes en cola y filas fallidas, desde la base local. */
    fun observarEstadoEnvio(): Flow<EstadoEnvio>

    /** Sube lo pendiente fila por fila y luego trae la lista del servidor. En ese orden. */
    suspend fun sincronizar(): ResumenSync

    /** Devuelve las filas fallidas a la cola (intentos = 0). */
    suspend fun reintentarFallidos()

    /** Quita un producto que nunca llegó a PharmaSoft (id negativo). */
    suspend fun descartarPendiente(id: Long)

    suspend fun obtener(id: Long): Producto

    /** Guarda en local; intenta enviar si hay conexión. Sin red devuelve el producto con id negativo. */
    suspend fun registrar(producto: Producto): Producto

    suspend fun actualizar(producto: Producto): Producto

    suspend fun eliminar(id: Long)

    /** Sesión 14: filas que PharmaSoft todavía no conoce (en cola o fallidas). */
    suspend fun cambiosSinEnviar(): Int

    /** Sesión 14: deja la base vacía al cerrar sesión voluntariamente. */
    suspend fun borrarDatosLocales()
}
