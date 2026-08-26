package pe.edu.upeu.pharmamobile

import pe.edu.upeu.pharmamobile.domain.model.Cliente
import pe.edu.upeu.pharmamobile.domain.model.DetallePedido
import pe.edu.upeu.pharmamobile.domain.model.EstadoPedido
import pe.edu.upeu.pharmamobile.domain.model.Pedido
import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Pasos 2, 3 y 5 de la guia practica: null-safety, data classes y sealed classes.
 */
class DominioModeloTest {

    @Test
    fun telefonoNuloDevuelveValorPorDefecto() {
        val cliente = Cliente(
            id = 1L,
            nombre = "Farmacia Central",
            correo = "ventas@central.pe",
            telefono = null
        )
        println("Telefono: ${cliente.obtenerTelefono()}")
        assertEquals(expected = "No registrado", actual = cliente.obtenerTelefono())
    }

    @Test
    fun productoInvalidoLanzaExcepcion() {
        assertFailsWith<IllegalArgumentException> {
            Producto(id = 4L, nombre = "  ", precio = 5.0, stock = 10)
        }
        assertFailsWith<IllegalArgumentException> {
            Producto(id = 5L, nombre = "Aspirina", precio = -1.0, stock = 10)
        }
    }

    @Test
    fun pedidoCalculaTotalYDescribeSuEstado() {
        val paracetamol = Producto(id = 1L, nombre = "Paracetamol", precio = 8.50, stock = 100)
        val ibuprofeno = Producto(id = 2L, nombre = "Ibuprofeno", precio = 12.00, stock = 50)

        val pedido = Pedido(
            id = 1L,
            cliente = "Farmacia Central",
            detalles = listOf(
                DetallePedido(producto = paracetamol, cantidad = 2),
                DetallePedido(producto = ibuprofeno, cantidad = 1)
            ),
            estado = EstadoPedido.Rechazado(motivo = "Stock insuficiente")
        )

        println("Total del pedido: ${pedido.total()} | Estado: ${pedido.descripcionEstado()}")
        assertEquals(expected = 29.0, actual = pedido.total())
        assertEquals(expected = 3, actual = pedido.cantidadItems())
        assertEquals(
            expected = "Rechazado: Stock insuficiente",
            actual = pedido.descripcionEstado()
        )
    }

    @Test
    fun disminuirStockDevuelveUnaCopia() {
        val original = Producto(id = 1L, nombre = "Paracetamol", precio = 8.50, stock = 100)
        val vendido = original.disminuirStock(cantidad = 10)

        assertEquals(expected = 100, actual = original.stock)
        assertEquals(expected = 90, actual = vendido.stock)
    }
}