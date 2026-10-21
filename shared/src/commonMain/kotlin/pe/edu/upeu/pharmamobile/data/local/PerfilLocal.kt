package pe.edu.upeu.pharmamobile.data.local

import pe.edu.upeu.pharmamobile.db.PharmaDb
import pe.edu.upeu.pharmamobile.domain.model.Sesion

/** Usuario y permisos de la última sesión verificada, para abrir la app sin red. */
class PerfilLocal(base: PharmaDb) {

    private val consultas = base.perfilQueries

    fun guardar(sesion: Sesion) {
        consultas.guardarPerfil(
            usuario = sesion.usuario,
            nombre = sesion.nombreCompleto,
            permisos = sesion.permisos.joinToString(",")
        )
    }

    /** El token se pasa aparte: vive en el almacén seguro, no en la base. */
    fun leer(token: String): Sesion? =
        consultas.leerPerfil().executeAsOneOrNull()?.let { fila ->
            Sesion(
                token = token,
                usuario = fila.usuario,
                nombreCompleto = fila.nombre,
                permisos = fila.permisos.split(",").filter { it.isNotBlank() }.toSet()
            )
        }

    fun borrar() {
        consultas.borrarPerfil()
    }
}
