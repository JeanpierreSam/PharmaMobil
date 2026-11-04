package pe.edu.upeu.pharmamobile.permiso

import kotlinx.coroutines.suspendCancellableCoroutine
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

/** En iOS el primer «No permitir» es definitivo: solo queda Ajustes. */
class GestorPermisosIos : GestorPermisos {

    private val centro = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun estado(permiso: Permiso): EstadoPermiso {
        if (permiso != Permiso.NOTIFICACIONES) return EstadoPermiso.Denegado   // solo se implementó este
        val estado = suspendCancellableCoroutine { continuacion ->
            centro.getNotificationSettingsWithCompletionHandler { ajustes ->
                continuacion.resume(ajustes?.authorizationStatus ?: UNAuthorizationStatusNotDetermined)
            }
        }
        return when (estado) {
            UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional,
            UNAuthorizationStatusEphemeral -> EstadoPermiso.Concedido
            UNAuthorizationStatusDenied -> EstadoPermiso.DenegadoParaSiempre
            else -> EstadoPermiso.Denegado                                     // aún no se pidió
        }
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        if (permiso != Permiso.NOTIFICACIONES) return EstadoPermiso.Denegado
        suspendCancellableCoroutine { continuacion ->
            centro.requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            ) { _, _ -> continuacion.resume(Unit) }
        }
        return estado(permiso)
    }

    override fun abrirAjustes() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}
