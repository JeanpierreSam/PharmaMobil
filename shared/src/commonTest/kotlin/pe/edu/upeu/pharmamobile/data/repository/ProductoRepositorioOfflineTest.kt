package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.local.baseEnMemoria
import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Sesión 11: pruebas del repositorio offline. Cada una crea su propia base en
 * memoria (baseEnMemoria) y su propio PharmaSoft simulado: no comparten datos
 * ni dependen del orden de ejecución.
 */
class ProductoRepositorioOfflineTest {

    private val paracetamol = productoDelServidor(1L, "Paracetamol 500 mg", 4.5, 120)
    private val ibuprofeno = productoDelServidor(2L, "Ibuprofeno 400 mg", 6.8, 80)
    private val amoxicilina = Producto(0L, "Amoxicilina 500 mg", 12.5, 30)

    // 1. Sincronización con red ------------------------------------------------
    @Test
    fun prueba1_conRed_sincronizarLlenaLaBaseYElFlowEmiteLaListaCompleta() = runTest {
        val servidor = PharmaSoftFalso(paracetamol, ibuprofeno)
        val repo = repositorioOfflineCon(servidor, ProductoLocal(baseEnMemoria()))

        repo.sincronizar()

        // ORDER BY nombre: Ibuprofeno antes que Paracetamol
        assertEquals(
            listOf("Ibuprofeno 400 mg", "Paracetamol 500 mg"),
            repo.observar().first().map { it.nombre }
        )
    }

    // 2. Registro sin red (esencial) ------------------------------------------
    @Test
    fun prueba2_sinRed_elProductoQuedaPendienteYElFlowLoEmite() = runTest {
        val servidor = PharmaSoftFalso().apply { hayRed = false }
        val local = ProductoLocal(baseEnMemoria())
        val repo = repositorioOfflineCon(servidor, local)

        val guardado = repo.registrar(amoxicilina)

        assertTrue(guardado.id < 0, "Sin red el id debe ser local (negativo), fue ${guardado.id}")
        assertEquals(listOf(guardado.id), local.pendientes().map { it.id })            // marca de pendiente
        assertEquals(listOf("Amoxicilina 500 mg"), repo.observar().first().map { it.nombre })  // la pantalla lo ve
    }

    // 3. Envío al reconectar ---------------------------------------------------
    @Test
    fun prueba3_alReconectar_sincronizarEnviaLoPendienteYLaFilaDejaDeEstarMarcada() = runTest {
        val servidor = PharmaSoftFalso().apply { hayRed = false }
        val local = ProductoLocal(baseEnMemoria())
        val repo = repositorioOfflineCon(servidor, local)
        repo.registrar(amoxicilina)

        servidor.hayRed = true
        repo.sincronizar()

        assertTrue(local.pendientes().isEmpty(), "No debe quedar nada marcado como pendiente")
        assertEquals(listOf("Amoxicilina 500 mg"), servidor.productos.map { it.nombre })  // llegó a PharmaSoft
        assertEquals(100L, repo.observar().first().single().id)                         // con el id real
    }
}
