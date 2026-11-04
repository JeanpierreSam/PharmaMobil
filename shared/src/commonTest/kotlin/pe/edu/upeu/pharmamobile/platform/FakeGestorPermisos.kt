package pe.edu.upeu.pharmamobile.platform

import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso

/** La ventaja de haber declarado el gestor como interfaz: se prueba sin Activity ni emulador. */
class FakeGestorPermisos(
    var estadoActual: EstadoPermiso = EstadoPermiso.Denegado,
    var respuestaAlPedir: EstadoPermiso = EstadoPermiso.Concedido
) : GestorPermisos {
    var solicitudes = 0
        private set
    var ajustesAbiertos = 0
        private set

    override suspend fun estado(permiso: Permiso): EstadoPermiso = estadoActual

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        solicitudes++
        estadoActual = respuestaAlPedir
        return estadoActual
    }

    override fun abrirAjustes() {
        ajustesAbiertos++
    }
}
