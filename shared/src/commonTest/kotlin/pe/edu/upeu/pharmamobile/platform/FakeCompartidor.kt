package pe.edu.upeu.pharmamobile.platform

import pe.edu.upeu.pharmamobile.domain.platform.Compartidor

/** Doble de la capacidad nativa: registra lo que se habría compartido. */
class FakeCompartidor : Compartidor {
    val textosCompartidos = mutableListOf<String>()

    override fun compartir(texto: String) {
        textosCompartidos += texto
    }
}
