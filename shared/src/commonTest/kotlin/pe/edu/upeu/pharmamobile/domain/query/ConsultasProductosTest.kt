package pe.edu.upeu.pharmamobile.domain.query

import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Cubre la regla de clasificación del Reto 02 (Sesión 4): Activos, Inactivos
 * y Bajo Stock son ejes independientes, por lo que un mismo producto puede
 * pertenecer simultáneamente a Bajo Stock y a Activos o a Inactivos.
 */
class ConsultasProductosTest {

    private val paracetamol = Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 100, activo = true)
    private val amoxicilina = Producto(id = 3L, nombre = "Amoxicilina", precio = 25.00, stock = 5, activo = true)
    private val loratadina = Producto(id = 4L, nombre = "Loratadina", precio = 12.50, stock = 0, activo = false)
    private val diclofenaco = Producto(id = 5L, nombre = "Diclofenaco", precio = 20.00, stock = 3, activo = true)

    private val inventario = listOf(paracetamol, amoxicilina, loratadina, diclofenaco)

    @Test
    fun activosFiltraSoloPorElCampoActivo() {
        assertEquals(
            expected = listOf(paracetamol, amoxicilina, diclofenaco),
            actual = inventario.activos()
        )
    }

    @Test
    fun inactivosFiltraSoloPorElCampoActivo() {
        assertEquals(expected = listOf(loratadina), actual = inventario.inactivos())
    }

    @Test
    fun stockIgualAlUmbralPerteneceABajoStock() {
        // Frontera de la regla estricta stock <= Producto.STOCK_MINIMO.
        assertTrue(amoxicilina.stock == Producto.STOCK_MINIMO)
        assertTrue(inventario.bajoStock().contains(amoxicilina))
    }

    @Test
    fun stockPorEncimaDelUmbralNoEsBajoStock() {
        val ibuprofeno = Producto(id = 2L, nombre = "Ibuprofeno", precio = 18.90, stock = 6, activo = true)
        assertTrue(!listOf(ibuprofeno).bajoStock().contains(ibuprofeno))
    }

    @Test
    fun bajoStockEsTransversalAActivosEInactivos() {
        val bajoStock = inventario.bajoStock()

        // Amoxicilina (activa) y Loratadina (inactiva) están ambas en Bajo Stock.
        assertTrue(bajoStock.contains(amoxicilina))
        assertTrue(bajoStock.contains(loratadina))
        assertTrue(bajoStock.contains(diclofenaco))

        // Loratadina aparece tanto en Inactivos como en Bajo Stock a la vez.
        assertTrue(inventario.inactivos().contains(loratadina))
    }
}
