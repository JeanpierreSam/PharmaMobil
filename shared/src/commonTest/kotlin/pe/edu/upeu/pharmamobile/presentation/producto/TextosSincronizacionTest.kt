package pe.edu.upeu.pharmamobile.presentation.producto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TextosSincronizacionTest {

    @Test
    fun porEnviar_singularPluralYNada() {
        assertNull(textoPorEnviar(0))
        assertEquals("1 cambio por enviar", textoPorEnviar(1))
        assertEquals("3 cambios por enviar", textoPorEnviar(3))
    }

    @Test
    fun fallidas_singularYPlural() {
        assertNull(textoFallidas(0))
        assertEquals("1 cambio no se pudo enviar", textoFallidas(1))
        assertEquals("2 cambios no se pudieron enviar", textoFallidas(2))
    }

    @Test
    fun lineaEstado_uneElAvisoYLosPendientes() {
        assertEquals(
            "Sin conexión: mostrando datos guardados · 3 cambios por enviar",
            lineaEstado("Sin conexión: mostrando datos guardados", 3)
        )
        assertNull(lineaEstado(null, 0))
    }
}
