package pe.edu.upeu.pharmamobile.presentation.producto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobile.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobile.platform.FakeMonitorRed
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ReconexionTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun alVolverLaRed_sincronizaSola() = runTest {
        val repo = FakeProductoRepository()
        val red = FakeMonitorRed(inicial = false)
        viewModelCon(repo, red)
        val inicial = repo.llamadasASincronizar          // la del init

        red.estado.value = true

        assertEquals(inicial + 1, repo.llamadasASincronizar)
    }

    @Test
    fun perderLaRed_noDisparaSincronizacion() = runTest {
        val repo = FakeProductoRepository()
        val red = FakeMonitorRed(inicial = true)
        viewModelCon(repo, red)
        val inicial = repo.llamadasASincronizar

        red.estado.value = false

        assertEquals(inicial, repo.llamadasASincronizar)
    }

    @Test
    fun abrirConRed_noSincronizaDosVeces() = runTest {
        val repo = FakeProductoRepository()

        viewModelCon(repo, FakeMonitorRed(inicial = true))

        assertEquals(1, repo.llamadasASincronizar)       // solo la del init
    }
}
