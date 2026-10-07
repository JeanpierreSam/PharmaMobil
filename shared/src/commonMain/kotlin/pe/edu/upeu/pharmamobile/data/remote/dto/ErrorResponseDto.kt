package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Cuerpo de error de PharmaSoft (GlobalExceptionHandler). En un 400 de
 * validacion, [validationErrors] trae un mensaje por campo con las mismas
 * claves que el formulario: nombre, precio, stock, estado y categoriaId.
 */
@Serializable
data class ErrorResponseDto(
    val status: Int,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null,
    val validationErrors: Map<String, String>? = null
)
