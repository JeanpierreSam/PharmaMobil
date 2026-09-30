package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoDto

class ProductoApi(private val client: HttpClient) {
    suspend fun obtenerProductos(limite: Int = 10, desplazamiento: Int = 0): List<ProductoDto> =
        client.get("products") {
            parameter("limit", limite)
            parameter("offset", desplazamiento)
        }.body()

    // Documenta GET /products/{id} en la GAA; en la Sesión 8 lo usa obtener(id).
    suspend fun obtenerProducto(id: Int): ProductoDto = client.get("products/$id").body()
}
