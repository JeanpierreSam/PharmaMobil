package pe.edu.upeu.pharmamobile.data

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioRest
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProductoRepositorioRestTest {

    private fun repositorio(estado: HttpStatusCode, cuerpo: String = "[]"): ProductoRepositorioRest {
        val engine = MockEngine {
            respond(
                content = cuerpo,
                status = estado,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return ProductoRepositorioRest(ProductoApi(crearHttpClient(engine, "http://localhost/api/v1/")))
    }

    @Test
    fun listar_ignoraCamposDesconocidosYMapeaAlDominio() = runTest {
        val json = """
            [{"id":4,"title":"Paracetamol","slug":"paracetamol","price":10,
              "description":"x","images":[],"creationAt":"2026-09-30T00:00:00.000Z",
              "category":{"id":1,"name":"Salud","slug":"salud","image":"i"}}]
        """.trimIndent()

        val productos = repositorio(HttpStatusCode.OK, json).listar()

        assertEquals(1, productos.size)
        assertEquals(4L, productos.first().id)
        assertEquals("Paracetamol", productos.first().nombre)
        assertEquals(10.0, productos.first().precio)
    }

    @Test
    fun listar_descartaProductoQueNoCumpleReglasDelDominio() = runTest {
        val json = """[{"id":1,"title":"Valido","price":5},{"id":2,"title":"Gratis","price":0}]"""

        val productos = repositorio(HttpStatusCode.OK, json).listar()

        assertEquals(listOf("Valido"), productos.map { it.nombre })
    }

    @Test
    fun listar_con404_lanzaNoEncontrado() = runTest {
        val e = assertFailsWith<ErrorApiException> { repositorio(HttpStatusCode.NotFound).listar() }
        assertEquals(ErrorApi.NoEncontrado, e.error)
    }

    @Test
    fun listar_con500_lanzaServidor() = runTest {
        val e = assertFailsWith<ErrorApiException> { repositorio(HttpStatusCode.InternalServerError).listar() }
        assertEquals(ErrorApi.Servidor, e.error)
    }
}
