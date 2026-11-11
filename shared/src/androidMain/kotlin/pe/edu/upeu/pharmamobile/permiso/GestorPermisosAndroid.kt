package pe.edu.upeu.pharmamobile.permiso

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso

/**
 * shouldShowRequestPermissionRationale devuelve false en dos situaciones distintas:
 * antes de la primera solicitud y después de la denegación definitiva. Para
 * distinguirlas se recuerda en preferencias si el permiso ya se pidió alguna vez.
 *
 * | Situación             | Concedido | ¿Ya se pidió? | Explicar | Estado               |
 * |-----------------------|-----------|---------------|----------|----------------------|
 * | Primera vez           | No        | No            | No       | Denegado             |
 * | Denegó una vez        | No        | Sí            | Sí       | Denegado             |
 * | Denegó definitivamente| No        | Sí            | No       | DenegadoParaSiempre  |
 * | Concedido             | Sí        | —             | —        | Concedido            |
 */
class GestorPermisosAndroid(
    private val contexto: Context,
    private val preferencias: SharedPreferences
) : GestorPermisos {

    /** La Activity registra aquí su puente en onCreate y lo suelta en onDestroy. */
    var puente: PuentePermisos? = null

    override suspend fun estado(permiso: Permiso): EstadoPermiso {
        val nombre = nombreDeSistema(permiso) ?: return estadoSinDialogo(permiso)
        val concedido = ContextCompat.checkSelfPermission(contexto, nombre) ==
            PackageManager.PERMISSION_GRANTED
        if (concedido) return EstadoPermiso.Concedido
        val actividad = puente?.actividad ?: return EstadoPermiso.Denegado
        val yaSePidio = preferencias.getBoolean(CLAVE + nombre, false)
        val procedeExplicar = ActivityCompat.shouldShowRequestPermissionRationale(actividad, nombre)
        return when {
            !yaSePidio -> EstadoPermiso.Denegado
            procedeExplicar -> EstadoPermiso.Denegado
            else -> EstadoPermiso.DenegadoParaSiempre
        }
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        val nombre = nombreDeSistema(permiso) ?: return estadoSinDialogo(permiso)
        preferencias.edit().putBoolean(CLAVE + nombre, true).apply()
        val concedido = puente?.pedir(nombre) ?: false
        return if (concedido) EstadoPermiso.Concedido else estado(permiso)
    }

    /** Ajustes › Apps › PharmaMobil: desde ahí se activa cualquier permiso. */
    override fun abrirAjustes() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", contexto.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)   // Koin entrega el contexto de la aplicación
        contexto.startActivity(intent)
    }

    /**
     * Antes de Android 13 las notificaciones no se piden: vienen activadas y solo
     * pueden apagarse en Ajustes. Si están apagadas, el único camino es Ajustes.
     */
    private fun estadoSinDialogo(permiso: Permiso): EstadoPermiso =
        if (permiso == Permiso.NOTIFICACIONES &&
            !NotificationManagerCompat.from(contexto).areNotificationsEnabled()
        ) EstadoPermiso.DenegadoParaSiempre else EstadoPermiso.Concedido

    private fun nombreDeSistema(permiso: Permiso): String? = when (permiso) {
        // CAMARA y UBICACION quedan mapeados, pero NO están en el manifiesto:
        // pedirlos hoy devolvería denegado sin mostrar diálogo.
        Permiso.CAMARA -> Manifest.permission.CAMERA
        Permiso.UBICACION -> Manifest.permission.ACCESS_FINE_LOCATION
        Permiso.NOTIFICACIONES ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS
            else null
    }

    private companion object {
        const val CLAVE = "pedido_"
    }
}
