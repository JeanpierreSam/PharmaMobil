package pe.edu.upeu.pharmamobile.presentation.producto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Casos de prueba obligatorios de la actividad autonoma de la Sesion 03.
 *
 * Cubren la matriz completa de 7 casos sobre [validarYCrearProducto], que
 * concentra las reglas de negocio del formulario. Al ser una funcion pura
 * se puede verificar sin emulador ni interfaz grafica.
 */
class ValidacionProductoTest {

    /** Ejecuta la validacion y devuelve el mensaje de error obtenido. */
    private fun mensajeDeError(nombre: String, precio: String, stock: String): String {
        val resultado = validarYCrearProducto(
            id = 1L,
            nombre = nombre,
            precio = precio,
            stock = stock
        )
        assertIs<ResultadoRegistro.Invalido>(
            value = resultado,
            message = "Se esperaba un resultado invalido para [$nombre | $precio | $stock]"
        )
        return resultado.mensaje
    }

    // ---------------------------------------------------------------
    // Matriz de casos de prueba 1 al 7
    // ---------------------------------------------------------------

    @Test
    fun caso01_datosCorrectosRegistranElProducto() {
        val resultado = validarYCrearProducto(
            id = 1L,
            nombre = "Paracetamol 500 mg",
            precio = "8.50",
            stock = "100"
        )

        assertIs<ResultadoRegistro.Exitoso>(resultado)
        assertEquals(expected = 1L, actual = resultado.producto.id)
        assertEquals(expected = "Paracetamol 500 mg", actual = resultado.producto.nombre)
        assertEquals(expected = 8.50, actual = resultado.producto.precio)
        assertEquals(expected = 100, actual = resultado.producto.stock)
    }

    @Test
    fun caso02_nombreVacioEsRechazado() {
        assertEquals(
            expected = MensajesProducto.NOMBRE_OBLIGATORIO,
            actual = mensajeDeError(nombre = "", precio = "8.50", stock = "100")
        )
    }

    @Test
    fun caso03_precioNoNumericoEsRechazado() {
        assertEquals(
            expected = MensajesProducto.PRECIO_NO_NUMERICO,
            actual = mensajeDeError(nombre = "Ibuprofeno", precio = "abc", stock = "50")
        )
    }

    @Test
    fun caso04_precioEnCeroEsRechazado() {
        assertEquals(
            expected = MensajesProducto.PRECIO_NO_POSITIVO,
            actual = mensajeDeError(nombre = "Ibuprofeno", precio = "0", stock = "50")
        )
    }

    @Test
    fun caso05_stockNoEnteroEsRechazado() {
        assertEquals(
            expected = MensajesProducto.STOCK_NO_ENTERO,
            actual = mensajeDeError(nombre = "Amoxicilina", precio = "18.50", stock = "abc")
        )
    }

    @Test
    fun caso06_stockNegativoEsRechazado() {
        assertEquals(
            expected = MensajesProducto.STOCK_NEGATIVO,
            actual = mensajeDeError(nombre = "Amoxicilina", precio = "18.50", stock = "-5")
        )
    }

    @Test
    fun caso07_stockEnCeroEsValido() {
        val resultado = validarYCrearProducto(
            id = 7L,
            nombre = "Loratadina",
            precio = "10",
            stock = "0"
        )

        assertIs<ResultadoRegistro.Exitoso>(resultado)
        assertEquals(expected = 0, actual = resultado.producto.stock)
        assertEquals(expected = 10.0, actual = resultado.producto.precio)
        // Un stock en cero es un registro valido, pero el producto queda
        // marcado como no disponible por la regla del dominio.
        assertFalse(resultado.producto.estadoDisponible())
    }

    // ---------------------------------------------------------------
    // Reglas adicionales exigidas por la actividad
    // ---------------------------------------------------------------

    @Test
    fun nombreConSoloEspaciosEsRechazado() {
        // La regla es isNotBlank(), no isNotEmpty(): "   " tambien es invalido.
        assertEquals(
            expected = MensajesProducto.NOMBRE_OBLIGATORIO,
            actual = mensajeDeError(nombre = "     ", precio = "8.50", stock = "100")
        )
    }

    @Test
    fun precioNegativoEsRechazadoPorRango() {
        assertEquals(
            expected = MensajesProducto.PRECIO_NO_POSITIVO,
            actual = mensajeDeError(nombre = "Ibuprofeno", precio = "-3.50", stock = "50")
        )
    }

    @Test
    fun stockDecimalEsRechazadoPorNoSerEntero() {
        assertEquals(
            expected = MensajesProducto.STOCK_NO_ENTERO,
            actual = mensajeDeError(nombre = "Amoxicilina", precio = "18.50", stock = "10.5")
        )
    }

    @Test
    fun elNombreSeGuardaSinEspaciosSobrantes() {
        val resultado = validarYCrearProducto(
            id = 2L,
            nombre = "   Loratadina   ",
            precio = "10",
            stock = "5"
        )

        assertIs<ResultadoRegistro.Exitoso>(resultado)
        assertEquals(expected = "Loratadina", actual = resultado.producto.nombre)
    }

    @Test
    fun laValidacionRespetaElOrdenEstricto() {
        // Con nombre vacio Y precio invalido, debe ganar el error de nombre
        // porque se evalua primero.
        assertEquals(
            expected = MensajesProducto.NOMBRE_OBLIGATORIO,
            actual = mensajeDeError(nombre = "", precio = "abc", stock = "-5")
        )

        // Con nombre valido, el precio no numerico gana sobre el stock negativo.
        assertEquals(
            expected = MensajesProducto.PRECIO_NO_NUMERICO,
            actual = mensajeDeError(nombre = "Ibuprofeno", precio = "abc", stock = "-5")
        )

        // Conversion de stock antes que rango de stock.
        assertEquals(
            expected = MensajesProducto.STOCK_NO_ENTERO,
            actual = mensajeDeError(nombre = "Ibuprofeno", precio = "5.00", stock = "abc")
        )
    }

    // ---------------------------------------------------------------
    // Helpers que alimentan la propiedad isError de la interfaz
    // ---------------------------------------------------------------

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
