package pe.edu.upeu.pharmamobile.platform

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pe.edu.upeu.pharmamobile.domain.platform.Notificador

class NotificadorAndroid(private val contexto: Context) : Notificador {

    private val gestor = NotificationManagerCompat.from(contexto)

    @SuppressLint("MissingPermission")   // se comprueba con areNotificationsEnabled()
    override fun avisar(titulo: String, texto: String) {
        if (!gestor.areNotificationsEnabled()) return
        // Desde Android 8 toda notificación necesita un canal; en versiones previas no hace nada.
        gestor.createNotificationChannel(
            NotificationChannelCompat.Builder(CANAL, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("Stock bajo")
                .setDescription("Avisos de productos con pocas unidades")
                .build()
        )
        val notificacion = NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setAutoCancel(true)
            .build()
        gestor.notify(ID_STOCK_BAJO, notificacion)
    }

    private companion object {
        const val CANAL = "stock_bajo"
        const val ID_STOCK_BAJO = 1001
    }
}
