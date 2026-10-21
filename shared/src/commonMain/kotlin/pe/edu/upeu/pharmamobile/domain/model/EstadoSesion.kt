package pe.edu.upeu.pharmamobile.domain.model

/** Lo único que la interfaz necesita saber: si hay sesión o no. */
sealed interface EstadoSesion {
    data object Verificando : EstadoSesion
    data class Activa(val sesion: Sesion) : EstadoSesion
    /** [aviso]: por qué terminó (token expirado, usuario deshabilitado…), o null si el usuario salió. */
    data class Inactiva(val aviso: String? = null) : EstadoSesion
}
