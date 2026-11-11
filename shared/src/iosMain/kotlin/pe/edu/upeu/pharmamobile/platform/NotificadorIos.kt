package pe.edu.upeu.pharmamobile.platform

import pe.edu.upeu.pharmamobile.domain.platform.Notificador
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

/** Sin disparador: se entrega de inmediato. Con la app abierta, iOS no la muestra salvo con un delegado. */
class NotificadorIos : Notificador {
    override fun avisar(titulo: String, texto: String) {
        val contenido = UNMutableNotificationContent().apply {
            setTitle(titulo)
            setBody(texto)
        }
        val pedido = UNNotificationRequest.requestWithIdentifier("stock-bajo", contenido, null)
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(pedido, null)
    }
}
