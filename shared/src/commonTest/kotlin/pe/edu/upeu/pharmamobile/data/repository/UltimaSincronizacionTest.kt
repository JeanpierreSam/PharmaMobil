package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.local.baseEnMemoria
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Actividad autónoma 11, producto 3: el instante de la última sincronización
 * correcta. El reloj se inyecta para que la hora sea fija y la prueba repetible.
 */
class UltimaSincronizacionTest {

    private val paracetamol = productoDelServidor(1L, "Paracetamol 500 mg", 4.5, 120)

    @Test
    fun nuncaSincronizado_emiteNull() = runTest {
        val repo = repositorioOfflineCon(PharmaSoftFalso(paracetamol), ProductoLocal(baseEnMemoria()))

        assertNull(repo.observarUltimaSincronizacion().first())
    }

    @Test
    fun sincronizacionCorrecta_guardaElInstanteDelReloj() = runTest {
        val repo = repositorioOfflineCon(PharmaSoftFalso(paracetamol), ProductoLocal(baseEnMemoria())) { 5_000L }

        repo.sincronizar()

        assertEquals(5_000L, repo.observarUltimaSincronizacion().first())
    }

    @Test
    fun sincronizacionSinRed_noCambiaElInstanteAnterior() = runTest {
        val servidor = PharmaSoftFalso(paracetamol)
        var ahora = 5_000L
        val repo = repositorioOfflineCon(servidor, ProductoLocal(baseEnMemoria())) { ahora }
        repo.sincronizar()

        servidor.hayRed = false
        ahora = 9_000L
        assertFailsWith<ErrorApiException> { repo.sincronizar() }

        assertEquals(5_000L, repo.observarUltimaSincronizacion().first())   // sigue la última buena
    }
}
