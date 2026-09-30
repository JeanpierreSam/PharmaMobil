package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/**
 * Doble del repositorio para las pruebas: sin latencia real, con la misma
 * baja lógica que PharmaSoft y capaz de fallar a voluntad en cada operación.
 * [retardoMs] permite observar estados intermedios (Operacion.EnCurso).
 */
class FakeProductoRepository(
    productos: List<Producto> = emptyList(),
    var retardoMs: Long = 0L
) : ProductoRepository {

    val productos = productos.toMutableList()

    var fallaAlListar: Throwable? = null
    var fallaAlObtener: Throwable? = null
    var fallaAlRegistrar: Throwable? = null
    var fallaAlActualizar: Throwable? = null
    var fallaAlEliminar: Throwable? = null

    var llamadasAListar = 0
        private set

    override suspend fun listar(): List<Producto> {
        llamadasAListar++
        esperarOFallar(fallaAlListar)
        return productos.toList()
    }

    override suspend fun obtener(id: Long): Producto {
        esperarOFallar(fallaAlObtener)
        return productos.first { it.id == id }
    }

    override suspend fun registrar(producto: Producto): Producto {
        esperarOFallar(fallaAlRegistrar)
        val guardado = producto.copy(id = (productos.maxOfOrNull { it.id } ?: 0L) + 1L)
        productos += guardado
        return guardado
    }

    override suspend fun actualizar(producto: Producto): Producto {
        esperarOFallar(fallaAlActualizar)
        productos.replaceAll { if (it.id == producto.id) producto else it }
        return producto
    }

    override suspend fun eliminar(id: Long) {
        esperarOFallar(fallaAlEliminar)
        productos.replaceAll { if (it.id == id) it.copy(activo = false) else it }
    }

    private suspend fun esperarOFallar(falla: Throwable?) {
        if (retardoMs > 0) delay(retardoMs)
        falla?.let { throw it }
    }
}
