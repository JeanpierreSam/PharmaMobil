package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonObject
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.remote.dto.ErrorResponseDto

data class ResultadoPrueba(val prueba: String, val codigo: Int?, val mensaje: String)

/**
 * SOLO DEPURACIÓN. [clienteApp] es el mismo HttpClient de la app (con Auth);
 * [clienteSinAuth] no tiene el plugin, para poder enviar la cabecera
 * alterada, en minúscula o no enviarla.
 */
class DiagnosticoApi(
    private val clienteApp: HttpClient,
    private val clienteSinAuth: HttpClient,
    private val almacen: AlmacenSeguro,
    private val authApi: AuthApi
) {
    /** Escenario 4 y efecto 2: lo que hace la app real cuando no hay sesión. */
    suspend fun peticionDeLaAppSinSesion() =
        probar("GET /productos con el cliente de la app") { clienteApp.get("productos") { expectSuccess = false } }

    suspend fun tokenValido() = conCabecera("1. Token válido", almacen.leerToken()?.let { "Bearer $it" })
    suspend fun tokenAlteradoEnMemoria() = conCabecera("3. Token alterado", almacen.leerToken()?.let { "Bearer ${alterar(it)}" })
    suspend fun sinCabecera() = probar("4. Sin cabecera") { clienteSinAuth.get("productos") { expectSuccess = false } }
    suspend fun prefijoMinuscula() = conCabecera("5. Prefijo «bearer»", almacen.leerToken()?.let { "bearer $it" })

    /** Escenario 3 en la app real: altera el token del almacén; la siguiente petición de la app falla. */
    fun alterarTokenGuardado(): Boolean {
        val token = almacen.leerToken() ?: return false
        almacen.guardarToken(alterar(token))
        authApi.olvidarTokenEnCache()
        return true
    }

    /**
     * Mismo criterio que la colección de Postman del curso: las escrituras mandan
     * un cuerpo vacío «{}». Spring Security revisa el permiso ANTES de validar
     * el cuerpo, así que: sin permiso → 403; con permiso → 400 de validación.
     * Se prueba la autorización sin crear productos ni ventas en la base.
     */
    suspend fun matrizDePermisos(): List<ResultadoPrueba> {
        val token = almacen.leerToken() ?: return listOf(ResultadoPrueba("Matriz", null, "No hay token guardado"))
        val conToken: HttpRequestBuilder.() -> Unit = {
            expectSuccess = false
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return listOf(
            probar("Listar productos") { clienteSinAuth.get("productos") { conToken(); parameter("tamanio", 1) } },
            probar("Registrar producto") { clienteSinAuth.post("productos") { conToken(); setBody(CUERPO_VACIO) } },
            probar("Listar clientes") { clienteSinAuth.get("clientes") { conToken() } },
            probar("Registrar venta") { clienteSinAuth.post("ventas") { conToken(); setBody(CUERPO_VACIO) } },
            probar("Ver reportes") { clienteSinAuth.get("reportes/productos-mas-vendidos") { conToken() } },
            probar("Consultar /auth/me") { clienteSinAuth.get("auth/me") { conToken() } }
        )
    }

    private suspend fun conCabecera(nombre: String, valor: String?) =
        if (valor == null) ResultadoPrueba(nombre, null, "No hay token guardado")
        else probar(nombre) { clienteSinAuth.get("productos") { expectSuccess = false; header(HttpHeaders.Authorization, valor) } }

    private suspend fun probar(nombre: String, peticion: suspend () -> HttpResponse): ResultadoPrueba =
        try {
            val r = peticion()
            ResultadoPrueba(nombre, r.status.value, if (r.status.isSuccess()) "OK" else mensajeDe(r))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoPrueba(nombre, null, "Sin respuesta (${e::class.simpleName})")
        }

    private suspend fun mensajeDe(r: HttpResponse): String =
        runCatching { r.body<ErrorResponseDto>().message }.getOrNull() ?: r.status.description

    private companion object {
        val CUERPO_VACIO = JsonObject(emptyMap())   // se serializa como {}
    }
}

/**
 * Cambia el PRIMER carácter de la firma. No el último: en Base64URL el último
 * carácter puede llevar solo bits de relleno y el token seguiría siendo válido.
 */
internal fun alterar(token: String): String {
    val i = token.lastIndexOf('.') + 1
    val nuevo = if (token[i] == 'A') 'B' else 'A'
    return token.substring(0, i) + nuevo + token.substring(i + 1)
}
