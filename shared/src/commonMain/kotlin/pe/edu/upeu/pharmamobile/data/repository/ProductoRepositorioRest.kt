package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.data.mapper.toDomain
import pe.edu.upeu.pharmamobile.data.mapper.toRequest
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/**
 * Implementación contra PharmaSoft. Cada operación pasa por [ejecutarLlamada]:
 * si falla, sale una ErrorApiException y la capa de presentación nunca ve
 * excepciones de Ktor.
 */
class ProductoRepositorioRest(
    private val api: ProductoApi,
    private val categoriaPorDefecto: Long,
    /** Se invoca ante un 401: el token expiró, fue alterado o el usuario fue deshabilitado. */
    private val alPerderSesion: suspend (mensaje: String) -> Unit = {}
) : ProductoRepository {

    // Un registro que no cumpla las reglas de Producto (nombre vacío, precio 0)
    // se descarta en lugar de tumbar la lista completa.
    override suspend fun listar(): List<Producto> = llamar {
        api.listar().contenido.mapNotNull { runCatching { it.toDomain() }.getOrNull() }
    }

    override suspend fun obtener(id: Long): Producto = llamar { api.obtener(id).toDomain() }

    override suspend fun registrar(producto: Producto): Producto = llamar {
        api.crear(producto.toRequest(categoriaPorDefecto)).toDomain()
    }

    override suspend fun actualizar(producto: Producto): Producto = llamar {
        api.actualizar(producto.id, producto.toRequest(categoriaPorDefecto)).toDomain()
    }

    override suspend fun eliminar(id: Long) = llamar { api.eliminar(id) }

    private suspend fun <T> llamar(bloque: suspend () -> T): T {
        val resultado = ejecutarLlamada(bloque)
        val error = (resultado.exceptionOrNull() as? ErrorApiException)?.error
        if (error is ErrorApi.NoAutenticado) alPerderSesion(error.mensaje)
        return resultado.getOrThrow()
    }
}
