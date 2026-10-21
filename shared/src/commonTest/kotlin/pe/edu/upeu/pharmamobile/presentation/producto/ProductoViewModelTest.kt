package pe.edu.upeu.pharmamobile.presentation.producto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobile.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObservarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObservarUltimaSincronizacionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.SincronizarProductosUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Arma el ViewModel con todos sus casos de uso sobre el mismo repositorio falso. */
internal fun viewModelCon(repo: FakeProductoRepository) = ProductoViewModel(
    ObservarProductosUseCase(repo),
    SincronizarProductosUseCase(repo),
    ObtenerProductoUseCase(repo),
    RegistrarProductoUseCase(repo),
    ActualizarProductoUseCase(repo),
    EliminarProductoUseCase(repo),
    ObservarUltimaSincronizacionUseCase(repo)
)

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
        val vm = viewModelCon(FakeProductoRepository())
        assertIs<ProductoUiState.Fase.SinProductos>(vm.uiState.value.fase)
    }

    @Test
    fun repositorioConProductos_produceConProductos() = runTest {
        val vm = viewModelCon(FakeProductoRepository(listOf(Producto(1L, "Paracetamol", 15.50, 100, true))))
        assertIs<ProductoUiState.Fase.ConProductos>(vm.uiState.value.fase)
    }

    // Sesión 11: sin red ya no hay Fase.Error; la lista guardada se queda y se avisa.
    @Test
    fun sincronizacionSinRed_conservaLaListaYMuestraElAviso() = runTest {
        val repo = FakeProductoRepository(listOf(Producto(1L, "Paracetamol", 15.50, 100, true)))
            .apply { fallaAlSincronizar = ErrorApiException(ErrorApi.SinConexion) }
        val vm = viewModelCon(repo)
        assertIs<ProductoUiState.Fase.ConProductos>(vm.uiState.value.fase)
        assertEquals("Sin conexión: mostrando datos guardados", vm.uiState.value.avisoRed)
    }

    @Test
    fun precioCero_dejaErrorBajoPrecioSinLlamarAlRepositorio() = runTest {
        val repo = FakeProductoRepository()
        val vm = viewModelCon(repo)
        vm.nuevoProducto()
        vm.onNombreChange("Ibuprofeno")
        vm.onPrecioChange("0")
        vm.onStockChange("10")
        vm.guardar()
        assertEquals(MensajesProducto.PRECIO_NO_POSITIVO, vm.uiState.value.formulario.precioError)
        assertEquals(0, repo.productos.size)
    }
}
