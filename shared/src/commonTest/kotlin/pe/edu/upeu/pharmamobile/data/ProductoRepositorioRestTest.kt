package pe.edu.upeu.pharmamobile.data

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioRest
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

/**
 * Contrato de PharmaSoft simulado con MockEngine: formas reales del listado
 * paginado y del ErrorResponseDTO, tomadas de respuestas del backend local.
 */
class ProductoRepositorioRestTest {

    private var ultimoMetodo: HttpMethod? = null

    private fun repositorio(estado: HttpStatusCode, cuerpo: String = ""): ProductoRepositorioRest {
        val engine = MockEngine { peticion ->
            ultimoMetodo = peticion.method
            respond(
                content = cuerpo,
                status = estado,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return ProductoRepositorioRest(
            ProductoApi(crearHttpClient(engine, "http://localhost/api/v1/")),
            categoriaPorDefecto = 1L
        )
    }

    private fun pagina(vararg productos: String) =
        """{"contenido":[${productos.joinToString(",")}],"pagina":0,"tamanio":20,
            "totalElementos":${productos.size},"totalPaginas":1,"ultima":true}"""

    private fun error(status: Int, mensaje: String, validaciones: String = "null") =
        """{"timestamp":"2026-09-30T15:33:13","status":$status,"error":"x","message":"$mensaje",
            "path":"/api/v1/productos","validationErrors":$validaciones}"""

    private val paracetamol =
        """{"id":1,"nombre":"Paracetamol 500 mg","precio":4.5,"stock":120,"estado":true,"categoriaId":1,
            "categoriaNombre":"Analgésicos","fechaCreacion":"2026-09-30T15:33:12.936345","fechaModificacion":null}"""

    @Test
    fun listar_leeElEnvoltorioPaginadoEIgnoraLasFechas() = runTest {
        val productos = repositorio(HttpStatusCode.OK, pagina(paracetamol)).listar()

        assertEquals(listOf(Producto(1L, "Paracetamol 500 mg", 4.5, 120, true)), productos)
    }

    @Test
    fun listar_mapeaEstadoFalseAProductoInactivo() = runTest {
        val inactivo = """{"id":6,"nombre":"Clorfenamina 4 mg","precio":2.4,"stock":25,"estado":false}"""

        val productos = repositorio(HttpStatusCode.OK, pagina(inactivo)).listar()

        assertFalse(productos.single().activo)
    }

    @Test
    fun listar_descartaProductoQueNoCumpleReglasDelDominio() = runTest {
        val gratis = """{"id":2,"nombre":"Gratis","precio":0,"stock":1,"estado":true}"""

        val productos = repositorio(HttpStatusCode.OK, pagina(paracetamol, gratis)).listar()

        assertEquals(listOf("Paracetamol 500 mg"), productos.map { it.nombre })
    }

    @Test
    fun registrar_con400_devuelveValidacionPorCampo() = runTest {
        val cuerpo = error(
            400, "Existen errores de validación",
            """{"precio":"El precio debe ser mayor que cero","nombre":"El nombre debe tener entre 3 y 150 caracteres"}"""
        )

        val e = assertFailsWith<ErrorApiException> {
            repositorio(HttpStatusCode.BadRequest, cuerpo).registrar(Producto(0L, "Ab", 1.0, 1, true))
        }

        val validacion = e.error as ErrorApi.Validacion
        assertEquals("El nombre debe tener entre 3 y 150 caracteres", validacion.porCampo["nombre"])
        assertEquals("El precio debe ser mayor que cero", validacion.porCampo["precio"])
    }

    @Test
    fun actualizar_con404_lanzaNoEncontrado() = runTest {
        val cuerpo = error(404, "Producto no encontrado con id: 999999")

        val e = assertFailsWith<ErrorApiException> {
            repositorio(HttpStatusCode.NotFound, cuerpo).actualizar(Producto(999999L, "Xyz", 1.0, 1, true))
        }

        assertEquals(ErrorApi.NoEncontrado, e.error)
    }

    @Test
    fun eliminar_con409_lanzaConflictoConElMensajeDelServidor() = runTest {
        val cuerpo = error(409, "El producto Clorfenamina 4 mg ya se encuentra inactivo")

        val e = assertFailsWith<ErrorApiException> { repositorio(HttpStatusCode.Conflict, cuerpo).eliminar(6L) }

        assertEquals(ErrorApi.Conflicto("El producto Clorfenamina 4 mg ya se encuentra inactivo"), e.error)
    }

    @Test
    fun eliminar_con204SinCuerpo_noIntentaDeserializar() = runTest {
        repositorio(HttpStatusCode.NoContent).eliminar(1L)

        assertEquals(HttpMethod.Delete, ultimoMetodo)
    }

    @Test
    fun listar_con500_lanzaServidor() = runTest {
        val e = assertFailsWith<ErrorApiException> {
            repositorio(HttpStatusCode.InternalServerError, error(500, "Error interno")).listar()
        }
        assertEquals(ErrorApi.Servidor, e.error)
    }
}
