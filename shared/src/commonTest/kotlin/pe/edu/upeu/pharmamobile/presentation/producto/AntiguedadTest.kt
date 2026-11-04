package pe.edu.upeu.pharmamobile.presentation.producto

import kotlin.test.Test
import kotlin.test.assertEquals

/** Texto «Actualizado hace…»: función pura, se prueba con horas fijas. */
class AntiguedadTest {

    private val minuto = 60_000L
    private val ahora = 100L * 24 * 60 * minuto

    @Test
    fun sinSincronizacion_avisaQueNuncaSeSincronizo() {
        assertEquals("Nunca sincronizado: los datos pueden estar incompletos", textoAntiguedad(null, ahora))
    }

    @Test
    fun menosDeUnMinuto_diceHaceUnMomento() {
        assertEquals("Actualizado hace un momento", textoAntiguedad(ahora - 30_000L, ahora))
    }

    @Test
    fun minutos_usaSingularYPlural() {
        assertEquals("Actualizado hace 1 minuto", textoAntiguedad(ahora - minuto, ahora))
        assertEquals("Actualizado hace 45 minutos", textoAntiguedad(ahora - 45 * minuto, ahora))
    }

    @Test
    fun horas_redondeaHaciaAbajo() {
        assertEquals("Actualizado hace 2 horas", textoAntiguedad(ahora - 150 * minuto, ahora))
    }

    @Test
    fun dias_masDeVeinticuatroHoras() {
        assertEquals("Actualizado hace 2 días", textoAntiguedad(ahora - 2 * 24 * 60 * minuto, ahora))
    }
}
