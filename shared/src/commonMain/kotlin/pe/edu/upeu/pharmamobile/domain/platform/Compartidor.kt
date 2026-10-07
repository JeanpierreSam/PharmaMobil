package pe.edu.upeu.pharmamobile.domain.platform

/**
 * Capacidad de enviar un texto a otra app (WhatsApp, correo, notas...).
 * Es una interfaz y no un expect porque cada implementación necesita algo que
 * commonMain no conoce: un Context en Android, un controlador de vista en iOS.
 * Cada plataforma registra la suya en su platformModule.
 */
interface Compartidor {
    fun compartir(texto: String)
}
