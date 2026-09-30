package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.data.mapper.toDomain
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ProductoRepositorioRest(private val api: ProductoApi) : ProductoRepository {

    // Un registro de la API que no cumpla las reglas de Producto (nombre vacío, precio 0)
    // se descarta en lugar de tumbar la lista completa.
    override suspend fun listar(): List<Producto> =
        ejecutarLlamada {
            api.obtenerProductos().mapNotNull { runCatching { it.toDomain() }.getOrNull() }
        }.getOrThrow()

    override suspend fun registrar(nombre: String, precio: Double, stock: Int, activo: Boolean): Producto =
        throw UnsupportedOperationException("El registro remoto se implementa en la sesión 8")
}
