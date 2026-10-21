package pe.edu.upeu.pharmamobile.presentation.sesion

/** La sesión en sí vive en EstadoSesion; aquí solo el formulario. */
data class SesionUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val error: String? = null,   // fallo del intento de login
    val aviso: String? = null    // por qué terminó la sesión anterior (401)
) {
    val puedeIngresar: Boolean get() = usuario.isNotBlank() && clave.isNotBlank()
}
