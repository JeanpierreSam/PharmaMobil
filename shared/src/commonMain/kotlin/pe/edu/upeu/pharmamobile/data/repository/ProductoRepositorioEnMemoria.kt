package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos
import kotlin.random.Random

class ProductoRepositorioEnMemoria : ProductoRepository {

    private val productosIniciales = listOf(
        Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 100, activo = true),
        Producto(id = 2L, nombre = "Ibuprofeno", precio = 18.90, stock = 50, activo = true),
        Producto(id = 3L, nombre = "Amoxicilina", precio = 25.00, stock = 5, activo = true),
        Producto(id = 4L, nombre = "Loratadina", precio = 12.50, stock = 0, activo = false),
        Producto(id = 5L, nombre = "Diclofenaco", precio = 20.00, stock = 3, activo = true)
    )

    private val _productosFlow = MutableStateFlow(productosIniciales)
    val productosFlow: StateFlow<List<Producto>> = _productosFlow.asStateFlow()

    override suspend fun registrar(nombre: String, precio: Double, stock: Int, activo: Boolean): Producto {
        delay(Random.nextLong(300, 800))
        val siguienteId = (_productosFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val nuevo = Producto(id = siguienteId, nombre = nombre, precio = precio, stock = stock, activo = activo)
        _productosFlow.value = _productosFlow.value + nuevo
        return nuevo
    }

    override suspend fun listar(): List<Producto> {
        delay(Random.nextLong(300, 800))
        return _productosFlow.value
    }

    // --- Ejercicios de Flow de la Sesión 03, conservados porque DominioAsincronoTest los ejercita ---

    fun observarEstados(): Flow<String> = flow {
        emit("Iniciando")
        delay(300)
        emit("Finalizado")
    }

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
