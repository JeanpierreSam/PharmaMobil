package pe.edu.upeu.pharmamobile.platform

import pe.edu.upeu.pharmamobile.domain.platform.Notificador

class FakeNotificador : Notificador {
    val avisos = mutableListOf<Pair<String, String>>()
    override fun avisar(titulo: String, texto: String) {
        avisos += titulo to texto
    }
}
