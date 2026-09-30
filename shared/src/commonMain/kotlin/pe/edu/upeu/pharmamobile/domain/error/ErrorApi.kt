package pe.edu.upeu.pharmamobile.domain.error

sealed interface ErrorApi {
    data object NoEncontrado : ErrorApi
    data object Servidor : ErrorApi
    data object SinConexion : ErrorApi
    data object TiempoAgotado : ErrorApi
    // Sesión 8: Validacion(porCampo) y Conflicto(mensaje)
}

class ErrorApiException(val error: ErrorApi) : Exception()
