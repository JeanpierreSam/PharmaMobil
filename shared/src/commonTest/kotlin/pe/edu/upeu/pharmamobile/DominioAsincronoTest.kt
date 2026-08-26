package pe.edu.upeu.pharmamobile

import pe.edu.upeu.pharmamobile.data.ProductoRepositorySimulado
import pe.edu.upeu.pharmamobile.demo.mostrarResultado
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.query.buscarPorId
import pe.edu.upeu.pharmamobile.domain.query.disponibles
import pe.edu.upeu.pharmamobile.domain.query.nombres
import pe.edu.upeu.pharmamobile.domain.query.valorTotalInventario
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DominioAsincronoTest {

    private val repositorio = ProductoRepositorySimulado()

    @Test
    fun obtenerProductosDevuelveElCatalogo() = runTest {
        val productos = repositorio.obtenerProductos()
        println("Productos obtenidos: ${productos.size} -> ${productos.nombres()}")
        assertEquals(expected = 3, actual = productos.size)
    }

    @Test
    fun observarEstadosEmiteDosValores() = runTest {
        val estados = mutableListOf<String>()
        repositorio.observarEstados().collect { estado ->
            println("Estado emitido: $estado")
            estados += estado
        }
        assertEquals(expected = listOf("Iniciando", "Finalizado"), actual = estados)
    }

    @Test
    fun observarProductosEmiteTresListas() = runTest {
        val emisiones = repositorio.observarProductos().toList()
        emisiones.forEachIndexed { i, lista ->
            println("Emisión $i -> ${lista.size} productos, stock id 1: ${lista.buscarPorId(1L)?.stock}")
        }
        assertEquals(expected = 3, actual = emisiones.size)
        assertEquals(expected = 90, actual = emisiones.last().buscarPorId(1L)?.stock)
    }

    @Test
    fun cargarProductosEmiteCargandoYExito() = runTest {
        val recibidos = repositorio.cargarProductos().toList()
        recibidos.forEach { mostrarResultado(it) }
        assertTrue(recibidos.first() is ResultadoProductos.Cargando)
        assertTrue(recibidos.last() is ResultadoProductos.Exito)
    }

    @Test
    fun consultasSobreColecciones() = runTest {
        val productos = repositorio.obtenerProductos()

        println("Disponibles: ${productos.disponibles().size}")
        println("Nombres: ${productos.nombres()}")
        println("Producto 2: ${productos.buscarPorId(2L)?.nombre}")
        println("Valor total del inventario: ${productos.valorTotalInventario()}")

        assertEquals(expected = "Ibuprofeno", actual = productos.buscarPorId(2L)?.nombre)
        assertTrue(productos.buscarPorId(99L) == null)
    }

    @Test
    fun copyNoMutaElObjetoOriginal() {
        val original = Producto(id = 1L, nombre = "Paracetamol", precio = 8.50, stock = 100)
        val actualizado = original.copy(stock = 90)

        println("Original: ${original.stock} | Copia: ${actualizado.stock}")
        assertEquals(expected = 100, actual = original.stock)
        assertEquals(expected = 90, actual = actualizado.stock)
    }

    @Test
    fun cargarProductosPuedeTerminarEnError() = runTest {
        val recibidos = repositorio.cargarProductosConError().toList()
        recibidos.forEach { mostrarResultado(it) }
        assertTrue(recibidos.last() is ResultadoProductos.Error)
    }
}