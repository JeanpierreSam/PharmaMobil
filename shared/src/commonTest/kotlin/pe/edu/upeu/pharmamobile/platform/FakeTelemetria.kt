package pe.edu.upeu.pharmamobile.platform

import pe.edu.upeu.pharmamobile.domain.port.Telemetria

/** Doble de la telemetría: registra lo que se habría enviado a Firebase. */
class FakeTelemetria : Telemetria {
    val eventos = mutableListOf<Pair<String, Map<String, String>>>()
    val errores = mutableListOf<Pair<Throwable, Map<String, String>>>()

    override fun registrarError(e: Throwable, contexto: Map<String, String>) {
        errores += e to contexto
    }

    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        eventos += nombre to datos
    }
}
