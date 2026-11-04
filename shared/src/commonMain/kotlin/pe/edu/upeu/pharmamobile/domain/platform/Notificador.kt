package pe.edu.upeu.pharmamobile.domain.platform

/** Capacidad de mostrar un aviso del sistema. Lo implementa cada plataforma. */
interface Notificador {
    fun avisar(titulo: String, texto: String)
}
