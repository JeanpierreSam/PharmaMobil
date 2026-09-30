package pe.edu.upeu.pharmamobile.presentation.producto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private class RepositorioFalsoVacio : ProductoRepository {
    override suspend fun registrar(nombre: String, precio: Double, stock: Int, activo: Boolean) =
        error("no usado en esta prueba")
    override suspend fun listar(): List<Producto> = emptyList()
}

private class RepositorioFalsoConProductos : ProductoRepository {
    override suspend fun registrar(nombre: String, precio: Double, stock: Int, activo: Boolean) =
        error("no usado en esta prueba")
    override suspend fun listar(): List<Producto> = listOf(
        Producto(1L, "Paracetamol", 15.50, 100, true)
    )
}

private class RepositorioFalsoConError : ProductoRepository {
    override suspend fun registrar(nombre: String, precio: Double, stock: Int, activo: Boolean) =
        error("no usado en esta prueba")
    override suspend fun listar(): List<Producto> = throw IllegalStateException("Sin conexión con el servidor")
}

/**
 * Pruebas de flujo de la Sesion 05 (Reto 02, Seccion 4): verifican que el
 * ViewModel llegue a cada fase de [ProductoUiState.Fase] segun la respuesta
 * del repositorio, usando implementaciones falsas en vez de un emulador.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {

    // viewModelScope despacha en Dispatchers.Main, que no existe en un test
    // JVM/Native puro. Se sustituye por un dispatcher de test para que las
    // corrutinas lanzadas por el ViewModel corran de forma inmediata.
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun repositorioVacio_produceSinProductos() = runTest {
        val repo = RepositorioFalsoVacio()
        val vm = ProductoViewModel(RegistrarProductoUseCase(repo), repo)
        vm.cargarProductos()
        assertIs<ProductoUiState.Fase.SinProductos>(vm.uiState.value.fase)
    }

    @Test
    fun repositorioConProductos_produceConProductos() = runTest {
        val repo = RepositorioFalsoConProductos()
        val vm = ProductoViewModel(RegistrarProductoUseCase(repo), repo)
        vm.cargarProductos()
        assertIs<ProductoUiState.Fase.ConProductos>(vm.uiState.value.fase)
    }

    @Test
    fun repositorioQueFalla_produceError() = runTest {
        val repo = RepositorioFalsoConError()
        val vm = ProductoViewModel(RegistrarProductoUseCase(repo), repo)
        vm.cargarProductos()
        assertIs<ProductoUiState.Fase.Error>(vm.uiState.value.fase)
    }

    @Test
    fun precioCero_dejaMensajeDeErrorSinLlamarAlRepositorio() = runTest {
        val repo = RepositorioFalsoVacio()
        val vm = ProductoViewModel(RegistrarProductoUseCase(repo), repo)
        vm.registrar(nombre = "Ibuprofeno", precio = "0", stock = "10", activo = true)
        assertEquals(MensajesProducto.PRECIO_NO_POSITIVO, vm.uiState.value.mensaje)
    }
}
