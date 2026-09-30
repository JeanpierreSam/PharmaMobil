package pe.edu.upeu.pharmamobile.presentation.producto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobile.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUiState.Fase
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUiState.Operacion
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Actividad autónoma de la Sesión 08: transiciones de estado del ViewModel.
 *
 * Se usa StandardTestDispatcher (no Unconfined) para poder detenerse entre
 * pasos con runCurrent() y observar estados intermedios como Cargando y
 * Operacion.EnCurso, que con un dispatcher inmediato no llegan a verse.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTransicionesTest {

    private val dispatcher = StandardTestDispatcher()

    private val paracetamol = Producto(1L, "Paracetamol 500 mg", 4.5, 120, true)
    private val ibuprofeno = Producto(2L, "Ibuprofeno 400 mg", 6.8, 80, true)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cargaExitosa_pasaDeCargandoAConProductosConLaListaEsperada() = runTest(dispatcher) {
        val vm = viewModelCon(FakeProductoRepository(listOf(paracetamol, ibuprofeno)))

        assertEquals(Fase.Cargando, vm.uiState.value.fase)

        advanceUntilIdle()

        assertEquals(Fase.ConProductos(listOf(paracetamol, ibuprofeno)), vm.uiState.value.fase)
    }

    @Test
    fun listadoVacio_terminaEnSinProductosYNoEnConProductosVacio() = runTest(dispatcher) {
        val vm = viewModelCon(FakeProductoRepository(emptyList()))

        advanceUntilIdle()

        assertIs<Fase.SinProductos>(vm.uiState.value.fase)
    }

    @Test
    fun errorDeValidacion_dejaLosMensajesBajoCadaCampoSinPasarAFaseError() = runTest(dispatcher) {
        val repo = FakeProductoRepository(listOf(paracetamol)).apply {
            fallaAlRegistrar = ErrorApiException(
                ErrorApi.Validacion(
                    mapOf(
                        "nombre" to "El nombre debe tener entre 3 y 150 caracteres",
                        "precio" to "El precio debe ser mayor que cero"
                    )
                )
            )
        }
        val vm = viewModelCon(repo)
        advanceUntilIdle()

        vm.nuevoProducto()
        vm.onNombreChange("Ab")
        vm.onPrecioChange("0.001")
        vm.onStockChange("10")
        vm.guardar()
        advanceUntilIdle()

        val estado = vm.uiState.value
        assertEquals("El nombre debe tener entre 3 y 150 caracteres", estado.formulario.nombreError)
        assertEquals("El precio debe ser mayor que cero", estado.formulario.precioError)
        assertNull(estado.formulario.stockError)
        assertIs<Fase.ConProductos>(estado.fase)
        assertEquals(Operacion.Inactiva, estado.operacion)
    }

    @Test
    fun eliminacion_pasaPorEnCursoYVuelveAInactivaConLaListaRecargada() = runTest(dispatcher) {
        val repo = FakeProductoRepository(listOf(paracetamol, ibuprofeno), retardoMs = 100)
        val vm = viewModelCon(repo)
        advanceUntilIdle()
        val listadosAntes = repo.llamadasAListar

        vm.eliminar(paracetamol.id)
        runCurrent()

        assertEquals(Operacion.EnCurso(Operacion.Tipo.Eliminar), vm.uiState.value.operacion)
        assertIs<Fase.ConProductos>(vm.uiState.value.fase)   // la lista no desaparece mientras tanto

        advanceUntilIdle()

        val estado = vm.uiState.value
        assertEquals(Operacion.Inactiva, estado.operacion)
        assertEquals("Producto dado de baja", estado.mensajeExito)
        assertEquals(listadosAntes + 1, repo.llamadasAListar)
        assertFalse(estado.productos.first { it.id == paracetamol.id }.activo)   // baja lógica
    }

    @Test
    fun conflicto_quedaComoOperacionFallidaConElMensajeDelServidor() = runTest(dispatcher) {
        val repo = FakeProductoRepository(listOf(paracetamol)).apply {
            fallaAlEliminar = ErrorApiException(ErrorApi.Conflicto("El producto Paracetamol 500 mg ya se encuentra inactivo"))
        }
        val vm = viewModelCon(repo)
        advanceUntilIdle()

        vm.eliminar(paracetamol.id)
        advanceUntilIdle()

        assertEquals(
            Operacion.Fallida("El producto Paracetamol 500 mg ya se encuentra inactivo"),
            vm.uiState.value.operacion
        )
        assertIs<Fase.ConProductos>(vm.uiState.value.fase)
    }
}
