package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobile.data.remote.dto.UsuarioActualDto

/** Recurso /api/v1/auth de PharmaSoft. */
class AuthApi(private val client: HttpClient) {

    suspend fun login(request: LoginRequestDto): LoginResponseDto =
        client.post("auth/login") { setBody(request) }.body()

    suspend fun usuarioActual(): UsuarioActualDto = client.get("auth/me").body()

    /**
     * loadTokens deja el token en memoria. Sin esto: tras un login la primera sesión
     * funciona y las siguientes no; tras un logout la app sigue autenticada
     * (efecto 2 de la GAA).
     */
    fun olvidarTokenEnCache() {
        client.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
