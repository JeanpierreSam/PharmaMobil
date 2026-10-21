package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoResponseDto

/**
 * PharmaSoft simulado con el MockEngine de Ktor: guarda lo que recibe por POST,
 * lo devuelve en el GET y asigna ids reales desde 100. Sin emulador ni red.
 *
 * - [hayRed] = false → cada petición lanza IOException, igual que el modo avión.
 * - [rechazarAltas] = true → cada POST responde 409 (regla de negocio de PharmaSoft).
 */
internal class PharmaSoftFalso(vararg iniciales: ProductoResponseDto) {

    var hayRed = true
    var rechazarAltas = false
    val productos = iniciales.toMutableList()
    var postsRecibidos = 0
        private set

    private var siguienteId = 100L
    private val json = Json { ignoreUnknownKeys = true }
    private val cabeceras = headersOf(HttpHeaders.ContentType, "application/json")

    val motor = MockEngine { peticion ->
        if (!hayRed) throw IOException("Modo avión: no hay red")
        when {
            peticion.method == HttpMethod.Post && rechazarAltas -> respond(
                content = """{"status":409,"message":"Ya existe un producto con ese nombre"}""",
                status = HttpStatusCode.Conflict,
                headers = cabeceras
            )
            peticion.method == HttpMethod.Post -> {
                postsRecibidos++
                val cuerpo = (peticion.body as TextContent).text
                val pedido = json.decodeFromString(ProductoRequestDto.serializer(), cuerpo)
                val creado = ProductoResponseDto(
                    id = siguienteId++,
                    nombre = pedido.nombre,
                    precio = pedido.precio,
                    stock = pedido.stock,
                    estado = pedido.estado,
                    categoriaId = pedido.categoriaId
                )
                productos += creado
                respond(json.encodeToString(ProductoResponseDto.serializer(), creado), HttpStatusCode.Created, cabeceras)
            }
            else -> respond(pagina(), HttpStatusCode.OK, cabeceras)
        }
    }

    private fun pagina(): String {
        val contenido = productos.joinToString(",") { json.encodeToString(ProductoResponseDto.serializer(), it) }
        return """{"contenido":[$contenido],"pagina":0,"tamanio":20,""" +
            """"totalElementos":${productos.size},"totalPaginas":1,"ultima":true}"""
    }
}

/** El repositorio real de la app, con la base en memoria y PharmaSoft simulado. */
internal fun repositorioOfflineCon(servidor: PharmaSoftFalso, local: ProductoLocal, reloj: () -> Long = { 0L }) =
    ProductoRepositorioOffline(
        local = local,
        remoto = ProductoRepositorioRest(
            ProductoApi(crearHttpClient(servidor.motor, "http://localhost/api/v1/")),
            categoriaPorDefecto = 1L
        ),
        reloj = reloj
    )

internal fun productoDelServidor(id: Long, nombre: String, precio: Double, stock: Int) =
    ProductoResponseDto(id = id, nombre = nombre, precio = precio, stock = stock)
