package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.local.baseEnMemoria
import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BorrarDatosLocalesTest {

    @Test
    fun borrarTodo_dejaLaBaseComoRecienInstalada() = runTest {
        val local = ProductoLocal(baseEnMemoria())
        local.guardarSincronizado(Producto(10L, "Paracetamol 500 mg", 15.5, 100))
        local.guardarPendiente(Producto(0L, "Amoxicilina 500 mg", 12.5, 30))
        local.registrarSincronizacion(1_000L)
        val repo = repositorioOfflineCon(PharmaSoftFalso(), local)

        assertEquals(1, repo.cambiosSinEnviar())
        repo.borrarDatosLocales()

        assertTrue(local.observar().first().isEmpty())
        assertNull(local.observarUltimaSincronizacion().first())
        assertEquals(0, repo.cambiosSinEnviar())
    }
}
