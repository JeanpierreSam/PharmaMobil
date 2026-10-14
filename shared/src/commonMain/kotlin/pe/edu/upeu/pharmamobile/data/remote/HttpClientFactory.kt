package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro

/**
 * [almacen] nulo = cliente sin plugin Auth (pruebas con MockEngine de la S8 y,
 * en la Parte B, el cliente de diagnóstico). Los valores por defecto mantienen
 * compilando a ProductoRepositorioRestTest.
 */
fun crearHttpClient(
    engine: HttpClientEngine,
    urlBase: String,
    almacen: AlmacenSeguro? = null,
    esDepuracion: Boolean = true
): HttpClient = HttpClient(engine) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        })
    }
    if (almacen != null) {
        install(Auth) {
            bearer {
                // Se consulta una vez y queda en caché: tras login y logout hay que
                // llamar a clearToken() (lo hace AuthApi.olvidarTokenEnCache()).
                loadTokens { almacen.leerToken()?.let { BearerTokens(it, "") } }
                // El token viaja desde la primera petición, salvo en el login.
                sendWithoutRequest { peticion -> !peticion.url.encodedPath.endsWith("auth/login") }
            }
        }
    }
    install(Logging) {
        // println llega a Logcat en Android y a la consola de Xcode en iOS
        logger = object : Logger {
            override fun log(message: String) {
                println("Ktor: $message")
            }
        }
        level = if (esDepuracion) LogLevel.HEADERS else LogLevel.NONE
        sanitizeHeader(placeholder = "Bearer ***") { nombre -> nombre == HttpHeaders.Authorization }
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15000
        connectTimeoutMillis = 10000
    }
    defaultRequest {
        url(urlBase)
        contentType(ContentType.Application.Json)
    }
}
