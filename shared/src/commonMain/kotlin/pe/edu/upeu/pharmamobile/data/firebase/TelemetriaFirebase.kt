package pe.edu.upeu.pharmamobile.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.crashlytics.crashlytics
import pe.edu.upeu.pharmamobile.domain.port.Telemetria

/**
 * Único archivo que importa el SDK de Firebase. Los runCatching evitan que un
 * fallo de la telemetría (Firebase sin configurar, por ejemplo) tumbe la app.
 */
class TelemetriaFirebase : Telemetria {

    override fun registrarError(e: Throwable, contexto: Map<String, String>) {
        runCatching { Firebase.crashlytics.recordException(e, contexto) }
    }

    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        runCatching { Firebase.analytics.logEvent(nombre, datos) }
    }
}
