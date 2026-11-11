package pe.edu.upeu.pharmamobile.domain.platform

import kotlinx.coroutines.flow.Flow

/**
 * Sesión 14: si hay una red con salida. Emite el estado actual al suscribirse
 * y luego cada cambio, sin repetir valores. Cada plataforma registra el suyo.
 */
interface MonitorRed {
    val hayRed: Flow<Boolean>
}
