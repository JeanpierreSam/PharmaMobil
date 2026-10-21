package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/**
 * Doble del repositorio para las pruebas del ViewModel: sin latencia real, con
 * la misma baja lógica que PharmaSoft y capaz de fallar a voluntad.
 *
 * Sesión 11: igual que la base real, la «tabla» es un Flow; cada escritura
 * emite de nuevo y el ViewModel se entera sin pedir la lista.
 * [retardoMs] permite observar estados intermedios (Operacion.EnCurso).
 */
class FakeProductoRepository(
    productos: List<Producto> = emptyList(),
    var retardoMs: Long = 0L
) : ProductoRepository {

    private val tabla = MutableStateFlow(productos)

    /** Lo que hay ahora en la «base». */
    val productos: List<Producto> get() = tabla.value

    var fallaAlSincronizar: Throwable? = null
    var fallaAlObtener: Throwable? = null
    var fallaAlRegistrar: Throwable? = null
    var fallaAlActualizar: Throwable? = null
    var fallaAlEliminar: Throwable? = null

    var llamadasASincronizar = 0
        private set

    override fun observar(): Flow<List<Producto>> = tabla

    override suspend fun sincronizar() {
        llamadasASincronizar++
        esperarOFallar(fallaAlSincronizar)
    }

    override suspend fun obtener(id: Long): Producto {
        esperarOFallar(fallaAlObtener)
        return tabla.value.first { it.id == id }
    }

    override suspend fun registrar(producto: Producto): Producto {
        esperarOFallar(fallaAlRegistrar)
        val guardado = producto.copy(id = (tabla.value.maxOfOrNull { it.id } ?: 0L) + 1L)
        tabla.value = tabla.value + guardado
        return guardado
    }

    override suspend fun actualizar(producto: Producto): Producto {
        esperarOFallar(fallaAlActualizar)
        tabla.value = tabla.value.map { if (it.id == producto.id) producto else it }
        return producto
    }

    override suspend fun eliminar(id: Long) {
        esperarOFallar(fallaAlEliminar)
        tabla.value = tabla.value.map { if (it.id == id) it.copy(activo = false) else it }
    }

    private suspend fun esperarOFallar(falla: Throwable?) {
        if (retardoMs > 0) delay(retardoMs)
        falla?.let { throw it }
    }
}
