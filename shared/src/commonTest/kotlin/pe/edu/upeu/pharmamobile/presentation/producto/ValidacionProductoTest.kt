package pe.edu.upeu.pharmamobile.presentation.producto

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `ValidacionProducto.kt` ya no decide si el formulario se puede enviar
 * (eso lo hace `RegistrarProductoUseCase`, ver `RegistrarProductoUseCaseTest`
 * en domain/usecase); solo le quedan los helpers de resaltado en tiempo real
 * que usa [ProductoScreen] mientras el usuario escribe.
 */
class ValidacionProductoTest {

    @Test
    fun losHelpersDeErrorDetectanCadaCampoInvalido() {
        assertTrue(nombreEsInvalido(""))
        assertTrue(nombreEsInvalido("   "))
        assertFalse(nombreEsInvalido("Paracetamol"))

        assertTrue(precioEsInvalido("abc"))
        assertTrue(precioEsInvalido("0"))
        assertTrue(precioEsInvalido("-1"))
        assertFalse(precioEsInvalido("8.50"))

        assertTrue(stockEsInvalido("abc"))
        assertTrue(stockEsInvalido("-5"))
        assertFalse(stockEsInvalido("0"))
        assertFalse(stockEsInvalido("100"))
    }
}
