package pe.edu.upeu.pharmamobile.data

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.remote.AuthApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.EstadoSesion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class AlmacenEnMemoria(var token: String? = null) : AlmacenSeguro {
    override fun guardarToken(token: String) { this.token = token }
    override fun leerToken() = token
    override fun borrarToken() { token = null }
}

private val json = headersOf(HttpHeaders.ContentType, "application/json")

class AuthRepositorioRestTest {

    private fun repo(almacen: AlmacenSeguro, responder: MockRequestHandler) =
        AuthRepositorioRest(AuthApi(crearHttpClient(MockEngine(responder), "http://localhost/api/v1/", almacen)), almacen)

    @Test
    fun loginGuardaElTokenYActivaLaSesion() = runTest {
        val almacen = AlmacenEnMemoria()
        val r = repo(almacen) {
            respond(
                """{"token":"abc","tipo":"Bearer","expiraEnSegundos":3600,"username":"admin",
                   "nombreCompleto":"Carlos Ramirez","roles":["ADMIN"],"permisos":["CATALOGO_LEER"]}""",
                HttpStatusCode.OK, json
            )
        }
        val sesion = r.iniciarSesion(Credenciales("admin", "Admin123"))
        assertEquals("abc", almacen.leerToken())
        assertTrue(sesion.puede("CATALOGO_LEER"))
        assertIs<EstadoSesion.Activa>(r.estado.value)
    }

    @Test
    fun alReabrirConTokenGuardadoLaCabeceraViajaConBearer() = runTest {
        var cabecera: String? = null
        val r = repo(AlmacenEnMemoria("abc")) { peticion ->
            cabecera = peticion.headers[HttpHeaders.Authorization]
            respond("""{"username":"admin","empleadoNombre":"Carlos","permisos":[]}""", HttpStatusCode.OK, json)
        }
        r.sesionGuardada()
        assertEquals("Bearer abc", cabecera)
    }

    @Test
    fun tokenExpiradoAlReabrirBorraElAlmacenYDejaElAviso() = runTest {
        val almacen = AlmacenEnMemoria("viejo")
        val r = repo(almacen) {
            respond(
                """{"status":401,"error":"Unauthorized","message":"El token ha expirado. Inicie sesión nuevamente","path":"/api/v1/auth/me"}""",
                HttpStatusCode.Unauthorized, json
            )
        }
        assertNull(r.sesionGuardada())
        assertNull(almacen.leerToken())
        assertEquals("El token ha expirado. Inicie sesión nuevamente", (r.estado.value as EstadoSesion.Inactiva).aviso)
    }
}
