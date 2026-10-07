package pe.edu.upeu.pharmamobile.platform

import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

class CompartidorIos : Compartidor {

    override fun compartir(texto: String) {
        val presentador = controladorVisible() ?: return
        val controlador = UIActivityViewController(
            activityItems = listOf(texto),
            applicationActivities = null
        )
        // En iPad la hoja se muestra como popover y necesita un punto de anclaje.
        controlador.popoverPresentationController?.sourceView = presentador.view
        presentador.presentViewController(controlador, animated = true, completion = null)
    }

    /**
     * Controlador sobre el que se presenta la hoja. keyWindow de UIApplication está
     * deprecado desde iOS 13: se toma la ventana activa de la escena conectada y,
     * si ya hay algo presentado encima, se sube hasta el último controlador.
     */
    private fun controladorVisible(): UIViewController? {
        val ventana = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstNotNullOfOrNull { it.keyWindow }
        var controlador = ventana?.rootViewController ?: return null
        while (true) {
            controlador = controlador.presentedViewController ?: return controlador
        }
    }
}
