package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(val username: String, val password: String) {
    override fun toString() = "LoginRequestDto(username=$username, password=***)"
}

@Serializable
data class LoginResponseDto(
    val token: String,
    val tipo: String,
    val expiraEnSegundos: Long,
    val username: String,
    val nombreCompleto: String,
    val roles: List<String>,
    val permisos: List<String>
) {
    override fun toString() = "LoginResponseDto(username=$username, token=***)"
}

/** GET /auth/me (UsuarioResponseDTO). Solo se declaran los campos que usa la app. */
@Serializable
data class UsuarioActualDto(
    val username: String,
    val empleadoNombre: String? = null,
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList()
)
