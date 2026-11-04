package pe.edu.upeu.pharmamobile.presentation.avisos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.platform.FakeGestorPermisos
import pe.edu.upeu.pharmamobile.platform.FakeNotificador
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AvisosStockViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun alAbrir_consultaElEstadoSinPedirElPermiso() {
        val gestor = FakeGestorPermisos(estadoActual = EstadoPermiso.Denegado)

        val vm = AvisosStockViewModel(gestor, FakeNotificador())

        assertEquals(EstadoPermiso.Denegado, vm.estado.value)
        assertEquals(0, gestor.solicitudes)            // nunca se pide al abrir
    }

    @Test
    fun activarYConceder_permiteEnviarElAviso() {
        val gestor = FakeGestorPermisos(respuestaAlPedir = EstadoPermiso.Concedido)
        val notificador = FakeNotificador()
        val vm = AvisosStockViewModel(gestor, notificador)

        vm.activar()
        vm.avisarAhora(productosBajoStock = 3)

        assertEquals(EstadoPermiso.Concedido, vm.estado.value)
        assertEquals("3 productos tienen 5 unidades o menos.", notificador.avisos.single().second)
    }

    @Test
    fun denegadoParaSiempre_llevaAAjustesSinVolverAPedir() {
        val gestor = FakeGestorPermisos(estadoActual = EstadoPermiso.DenegadoParaSiempre)
        val vm = AvisosStockViewModel(gestor, FakeNotificador())

        vm.abrirAjustes()

        assertEquals(1, gestor.ajustesAbiertos)
        assertEquals(0, gestor.solicitudes)
    }

    @Test
    fun sinPermiso_noSeNotifica() {
        val notificador = FakeNotificador()
        val vm = AvisosStockViewModel(FakeGestorPermisos(estadoActual = EstadoPermiso.Denegado), notificador)

        vm.avisarAhora(productosBajoStock = 2)

        assertTrue(notificador.avisos.isEmpty())
    }
}
