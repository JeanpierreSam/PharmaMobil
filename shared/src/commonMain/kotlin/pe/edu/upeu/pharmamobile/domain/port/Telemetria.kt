package pe.edu.upeu.pharmamobile.domain.port

/**
 * Lo que el dominio necesita del proveedor de telemetría. Si mañana se cambia
 * Firebase por otro servicio, cambia una clase de la capa de datos y nada más.
 * Regla: contexto técnico (pantalla, operación, código), nunca datos personales
 * ni el token: esto termina en un servicio de terceros.
 */
interface Telemetria {
    fun registrarError(e: Throwable, contexto: Map<String, String> = emptyMap())
    fun registrarEvento(nombre: String, datos: Map<String, String> = emptyMap())
}
