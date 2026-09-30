package pe.edu.upeu.pharmamobile.domain.error

sealed interface ErrorApi {
    /** 400: un mensaje por campo, con las mismas claves que el formulario. */
    data class Validacion(val porCampo: Map<String, String>) : ErrorApi
    data object NoEncontrado : ErrorApi
    /** 409: regla de negocio del backend (nombre duplicado, producto ya inactivo). */
    data class Conflicto(val mensaje: String) : ErrorApi
    data object Servidor : ErrorApi
    data object SinConexion : ErrorApi
    data object TiempoAgotado : ErrorApi
}

class ErrorApiException(val error: ErrorApi) : Exception()
