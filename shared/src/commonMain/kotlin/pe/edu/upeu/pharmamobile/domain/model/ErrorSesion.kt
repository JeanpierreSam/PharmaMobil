package pe.edu.upeu.pharmamobile.domain.model

/**
 * Los casos 401 llevan el mensaje literal de PharmaSoft: así la pantalla de login
 * puede mostrar «El token ha expirado…» o «El usuario está deshabilitado» tal cual.
 */
sealed interface ErrorSesion {
    data object CredencialesInvalidas : ErrorSesion
    data class SesionExpirada(val mensaje: String) : ErrorSesion
    data class NoAutenticado(val mensaje: String) : ErrorSesion   // sin token, alterado, deshabilitado
    data object SinPermiso : ErrorSesion
    data object SinConexion : ErrorSesion
}

class ErrorSesionException(val error: ErrorSesion) : Exception()
