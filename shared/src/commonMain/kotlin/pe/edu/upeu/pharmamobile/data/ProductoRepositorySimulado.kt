package pe.edu.upeu.pharmamobile.data

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductoRepositorySimulado {

    // Paso 3: fuente temporal de productos simulados
    private val productosSimulados = listOf(
        Producto(id = 1L, nombre = "Paracetamol", precio = 8.50, stock = 100),
        Producto(id = 2L, nombre = "Ibuprofeno", precio = 12.00, stock = 50),
        Producto(id = 3L, nombre = "Amoxicilina", precio = 18.50, stock = 20)
    )

    // Pasos 4-5: suspend devuelve UN solo valor tras simular latencia
    suspend fun obtenerProductos(): List<Producto> {
        delay(1000) // Simula espera de red
        return productosSimulados
    }

    suspend fun buscarProducto(id: Long): Producto? {
        delay(300)
        return productosSimulados.find { it.id == id }
    }

    // Pasos 9-11: Flow básico con emit()
    fun observarEstados(): Flow<String> = flow {
        emit("Iniciando")
        delay(1000)
        emit("Finalizado")
    }

    // Pasos 12-14: Flow del inventario + copy() para simular cambio de stock
    fun observarProductos(): Flow<List<Producto>> = flow {
        emit(emptyList())
        delay(1000)
        emit(productosSimulados)
        delay(1000)
        emit(
            productosSimulados.map { producto ->
                if (producto.id == 1L) producto.copy(stock = 90) else producto
            }
        )
    }

    // Pasos 15-17: Flow + sealed class (Cargando -> Éxito)
    fun cargarProductos(): Flow<ResultadoProductos> = flow {
        emit(ResultadoProductos.Cargando)
        delay(1000)
        emit(ResultadoProductos.Exito(productosSimulados))
    }

    // Rama de error, para demostrar el when exhaustivo completo
    fun cargarProductosConError(): Flow<ResultadoProductos> = flow {
        emit(ResultadoProductos.Cargando)
        delay(1000)
        emit(ResultadoProductos.Error("Sin conexión con el servidor"))
    }
}