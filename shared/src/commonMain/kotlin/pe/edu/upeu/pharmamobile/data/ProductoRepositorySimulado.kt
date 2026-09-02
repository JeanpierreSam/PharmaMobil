package pe.edu.upeu.pharmamobile.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos

class ProductoRepositorySimulado {

    // Lista inicial del avance autónomo S4 (Reto 02)
    private val productosIniciales = listOf(
        Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 100, activo = true),
        Producto(id = 2L, nombre = "Ibuprofeno", precio = 18.90, stock = 50, activo = true),
        Producto(id = 3L, nombre = "Amoxicilina", precio = 25.00, stock = 5, activo = true),
        Producto(id = 4L, nombre = "Loratadina", precio = 12.50, stock = 0, activo = false),
        Producto(id = 5L, nombre = "Diclofenaco", precio = 20.00, stock = 3, activo = true)
    )

    private val _productosFlow = MutableStateFlow(productosIniciales)
    val productosFlow: StateFlow<List<Producto>> = _productosFlow.asStateFlow()

    fun obtenerProductosSync(): List<Producto> {
        return _productosFlow.value
    }

    fun agregarProducto(producto: Producto) {
        _productosFlow.value = _productosFlow.value + producto
    }

    suspend fun obtenerProductos(): List<Producto> {
        delay(300)
        return _productosFlow.value
    }

    suspend fun buscarProducto(id: Long): Producto? {
        delay(100)
        return _productosFlow.value.find { it.id == id }
    }

    fun observarEstados(): Flow<String> = flow {
        emit("Iniciando")
        delay(300)
        emit("Finalizado")
    }

    // Flow finito para demostración y pruebas asíncronas
    fun observarProductos(): Flow<List<Producto>> = flow {
        emit(emptyList())
        delay(100)
        emit(_productosFlow.value)
        delay(100)
        emit(
            _productosFlow.value.map { producto ->
                if (producto.id == 1L) producto.copy(stock = 90) else producto
            }
        )
    }

    fun cargarProductos(): Flow<ResultadoProductos> = flow {
        emit(ResultadoProductos.Cargando)
        delay(100)
        emit(ResultadoProductos.Exito(_productosFlow.value))
    }

    fun cargarProductosConError(): Flow<ResultadoProductos> = flow {
        emit(ResultadoProductos.Cargando)
        delay(100)
        emit(ResultadoProductos.Error("Sin conexión con el servidor"))
    }
}
