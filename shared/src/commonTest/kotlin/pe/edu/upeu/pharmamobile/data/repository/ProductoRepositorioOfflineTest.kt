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

    // 4. La bajada no borra lo pendiente (esencial) -----------------------------
    @Test
    fun prueba4_traerLaListaDelServidorConservaLoCreadoSinConexion() = runTest {
        // PharmaSoft está en línea pero rechaza el alta (409): la subida falla y aun
        // así la bajada se ejecuta. Es justo el momento en que un borrado de la
        // tabla entera se llevaría el trabajo del usuario.
        val servidor = PharmaSoftFalso(paracetamol).apply { rechazarAltas = true }
        val local = ProductoLocal(baseEnMemoria())
        val repo = repositorioOfflineCon(servidor, local)
        val pendiente = local.guardarPendiente(amoxicilina)

        repo.sincronizar()

        assertEquals(setOf(1L, pendiente.id), repo.observar().first().map { it.id }.toSet())
        assertEquals(listOf(pendiente.id), local.pendientes().map { it.id })
    }

    // 5. Identificadores sin colisión -----------------------------------------
    @Test
    fun prueba5_productosLocalesYDelServidorConvivenSinSobrescribirse() = runTest {
        val servidor = PharmaSoftFalso(paracetamol)                 // id 1 en PharmaSoft
        val local = ProductoLocal(baseEnMemoria())
        val repo = repositorioOfflineCon(servidor, local)
        repo.sincronizar()

        servidor.hayRed = false
        val primero = repo.registrar(amoxicilina)
        val segundo = repo.registrar(Producto(0L, "Loratadina 10 mg", 3.2, 40))

        assertEquals(listOf(-1L, -2L), listOf(primero.id, segundo.id))
        val enLaBase = repo.observar().first()
        assertEquals(3, enLaBase.size)
        assertEquals(setOf(1L, -1L, -2L), enLaBase.map { it.id }.toSet())
        assertEquals("Paracetamol 500 mg", enLaBase.first { it.id == 1L }.nombre)    // nadie lo pisó
    }

    // 6. Sincronizar dos veces no duplica -------------------------------------
    @Test
    fun prueba6_sincronizarDosVecesSeguidasDejaLaMismaCantidadDeFilas() = runTest {
        val servidor = PharmaSoftFalso(paracetamol, ibuprofeno).apply { hayRed = false }
        val local = ProductoLocal(baseEnMemoria())
        val repo = repositorioOfflineCon(servidor, local)
        repo.registrar(amoxicilina)
        servidor.hayRed = true

        repo.sincronizar()
        val trasLaPrimera = repo.observar().first().size
        repo.sincronizar()

        assertEquals(3, trasLaPrimera)
        assertEquals(3, repo.observar().first().size)
        assertEquals(1, servidor.postsRecibidos)          // lo pendiente se envió una sola vez
        assertEquals(3, servidor.productos.size)
    }
}
