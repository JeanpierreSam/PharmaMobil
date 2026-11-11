package pe.edu.upeu.pharmamobile.presentation.detalle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobile.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.comoTextoParaCompartir
import pe.edu.upeu.pharmamobile.platform.FakeCompartidor
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Sesión 09: la capacidad nativa llega por inyección, así que en commonTest
 * se sustituye por [FakeCompartidor] sin emulador ni simulador.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetalleProductoViewModelTest {

    private val paracetamol = Producto(id = 1, nombre = "Paracetamol 500 mg", precio = 8.5, stock = 100)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repo: FakeProductoRepository, compartidor: FakeCompartidor = FakeCompartidor()) =
        DetalleProductoViewModel(ObtenerProductoUseCase(repo), compartidor)

    @Test
    fun abrir_cargaElProductoConElPrecioFormateado() = runTest {
        val vm = viewModel(FakeProductoRepository(listOf(paracetamol)))

        vm.abrir(1)

        val listo = assertIs<DetalleUiState.Listo>(vm.estado.value)
        assertEquals("Paracetamol 500 mg", listo.ui.nombre)
        assertTrue(listo.ui.precio.contains("8.50"), "precio formateado: ${listo.ui.precio}")
    }

    @Test
    fun abrir_conFalloDelServidor_quedaEnError() = runTest {
        val repo = FakeProductoRepository(listOf(paracetamol)).apply {
            fallaAlObtener = IllegalStateException("sin conexión")
        }
        val vm = viewModel(repo)

        vm.abrir(1)

        assertEquals(DetalleUiState.Error("sin conexión"), vm.estado.value)
    }

    @Test
    fun compartir_enviaAlCompartidorElTextoArmadoEnComun() {
        val compartidor = FakeCompartidor()
        val vm = viewModel(FakeProductoRepository(), compartidor)

        vm.compartir(paracetamol)

        val texto = compartidor.textosCompartidos.single()
        assertEquals(paracetamol.comoTextoParaCompartir(), texto)
        assertTrue(texto.startsWith("Paracetamol 500 mg — "))
        assertTrue(texto.endsWith("Stock: 100"))
    }

    @Test
    fun cerrar_vuelveACerrado() = runTest {
        val vm = viewModel(FakeProductoRepository(listOf(paracetamol)))
        vm.abrir(1)

        vm.cerrar()

        assertEquals(DetalleUiState.Cerrado, vm.estado.value)
    }
}
