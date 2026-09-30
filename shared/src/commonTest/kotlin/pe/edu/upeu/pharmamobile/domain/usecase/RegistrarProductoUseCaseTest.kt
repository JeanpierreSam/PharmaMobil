package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Matriz de casos obligatoria de la Sesion 03, migrada aqui porque la logica
 * de decision (antes en `ValidacionProducto.validarYCrearProducto`) ahora
 * vive en `RegistrarProductoUseCase` (Sesion 05, Reto 01). El id ya no lo
 * calcula la prueba: lo asigna el repositorio falso, igual que en produccion.
 */
class RegistrarProductoUseCaseTest {

    private class RepositorioFalso : ProductoRepository {
        private val productos = mutableListOf<Producto>()

        override suspend fun registrar(nombre: String, precio: Double, stock: Int, activo: Boolean): Producto {
            val nuevo = Producto(
                id = (productos.maxOfOrNull { it.id } ?: 0L) + 1L,
                nombre = nombre,
                precio = precio,
                stock = stock,
                activo = activo
            )
            productos += nuevo
            return nuevo
        }

        override suspend fun listar(): List<Producto> = productos
    }

    /**
     * Ejecuta el caso de uso y devuelve el nombre del error obtenido.
     *
     * `runTest` siempre devuelve `Unit` (su testBody es `suspend () -> Unit`),
     * asi que el mensaje se captura en una variable externa en vez de
     * devolverse directamente desde el bloque.
     */
    private fun nombreDelError(nombre: String, precio: String, stock: String): String? {
        var mensaje: String? = null
        runTest {
            val useCase = RegistrarProductoUseCase(RepositorioFalso())
            val resultado = useCase.invoke(nombre = nombre, precio = precio, stock = stock, activo = true)
            assertTrue(resultado.isFailure, "Se esperaba un resultado invalido para [$nombre | $precio | $stock]")
            mensaje = resultado.exceptionOrNull()?.message
        }
        return mensaje
    }

    // ---------------------------------------------------------------
    // Matriz de casos de prueba 1 al 7
    // ---------------------------------------------------------------

    @Test
    fun caso01_datosCorrectosRegistranElProducto() = runTest {
        val useCase = RegistrarProductoUseCase(RepositorioFalso())
        val resultado = useCase.invoke(nombre = "Paracetamol 500 mg", precio = "8.50", stock = "100", activo = true)

        val producto = resultado.getOrThrow()
        assertEquals(expected = 1L, actual = producto.id)
        assertEquals(expected = "Paracetamol 500 mg", actual = producto.nombre)
        assertEquals(expected = 8.50, actual = producto.precio)
        assertEquals(expected = 100, actual = producto.stock)
    }

    @Test
    fun caso02_nombreVacioEsRechazado() {
        assertEquals(
            expected = ErrorRegistroProducto.NombreObligatorio::class.simpleName,
            actual = nombreDelError(nombre = "", precio = "8.50", stock = "100")
        )
    }

    @Test
    fun caso03_precioNoNumericoEsRechazado() {
        assertEquals(
            expected = ErrorRegistroProducto.PrecioNoNumerico::class.simpleName,
            actual = nombreDelError(nombre = "Ibuprofeno", precio = "abc", stock = "50")
        )
    }

    @Test
    fun caso04_precioEnCeroEsRechazado() {
        assertEquals(
            expected = ErrorRegistroProducto.PrecioNoPositivo::class.simpleName,
            actual = nombreDelError(nombre = "Ibuprofeno", precio = "0", stock = "50")
        )
    }

    @Test
    fun caso05_stockNoEnteroEsRechazado() {
        assertEquals(
            expected = ErrorRegistroProducto.StockNoEntero::class.simpleName,
            actual = nombreDelError(nombre = "Amoxicilina", precio = "18.50", stock = "abc")
        )
    }

    @Test
    fun caso06_stockNegativoEsRechazado() {
        assertEquals(
            expected = ErrorRegistroProducto.StockNegativo::class.simpleName,
            actual = nombreDelError(nombre = "Amoxicilina", precio = "18.50", stock = "-5")
        )
    }

    @Test
    fun caso07_stockEnCeroEsValido() = runTest {
        val useCase = RegistrarProductoUseCase(RepositorioFalso())
        val resultado = useCase.invoke(nombre = "Loratadina", precio = "10", stock = "0", activo = true)

        val producto = resultado.getOrThrow()
        assertEquals(expected = 0, actual = producto.stock)
        assertEquals(expected = 10.0, actual = producto.precio)
        // Un stock en cero es un registro valido, pero el producto queda
        // marcado como no disponible por la regla del dominio.
        assertFalse(producto.estadoDisponible())
    }

    // ---------------------------------------------------------------
    // Reglas adicionales exigidas por la actividad
    // ---------------------------------------------------------------

    @Test
    fun nombreConSoloEspaciosEsRechazado() {
        // La regla es isNotBlank(), no isNotEmpty(): "   " tambien es invalido.
        assertEquals(
            expected = ErrorRegistroProducto.NombreObligatorio::class.simpleName,
            actual = nombreDelError(nombre = "     ", precio = "8.50", stock = "100")
        )
    }

    @Test
    fun precioNegativoEsRechazadoPorRango() {
        assertEquals(
            expected = ErrorRegistroProducto.PrecioNoPositivo::class.simpleName,
            actual = nombreDelError(nombre = "Ibuprofeno", precio = "-3.50", stock = "50")
        )
    }

    @Test
    fun stockDecimalEsRechazadoPorNoSerEntero() {
        assertEquals(
            expected = ErrorRegistroProducto.StockNoEntero::class.simpleName,
            actual = nombreDelError(nombre = "Amoxicilina", precio = "18.50", stock = "10.5")
        )
    }

    @Test
    fun elNombreSeGuardaSinEspaciosSobrantes() = runTest {
        val useCase = RegistrarProductoUseCase(RepositorioFalso())
        val resultado = useCase.invoke(nombre = "   Loratadina   ", precio = "10", stock = "5", activo = true)

        assertEquals(expected = "Loratadina", actual = resultado.getOrThrow().nombre)
    }

    @Test
    fun laValidacionRespetaElOrdenEstricto() {
        // Con nombre vacio Y precio invalido, debe ganar el error de nombre
        // porque se evalua primero.
        assertEquals(
            expected = ErrorRegistroProducto.NombreObligatorio::class.simpleName,
            actual = nombreDelError(nombre = "", precio = "abc", stock = "-5")
        )

        // Con nombre valido, el precio no numerico gana sobre el stock negativo.
        assertEquals(
            expected = ErrorRegistroProducto.PrecioNoNumerico::class.simpleName,
            actual = nombreDelError(nombre = "Ibuprofeno", precio = "abc", stock = "-5")
        )

        // Conversion de stock antes que rango de stock.
        assertEquals(
            expected = ErrorRegistroProducto.StockNoEntero::class.simpleName,
            actual = nombreDelError(nombre = "Ibuprofeno", precio = "5.00", stock = "abc")
        )
    }
}
