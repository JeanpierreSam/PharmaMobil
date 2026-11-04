package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.local.baseEnMemoria
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.EstadoEnvio
import pe.edu.upeu.pharmamobile.domain.model.MAX_INTENTOS_ENVIO
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResumenSync
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Sesión 13: el reintento tiene límite y memoria. Misma estrategia que la S11:
 * base en memoria por prueba y PharmaSoft simulado con MockEngine.
 */
class SincronizacionReintentosTest {

    private val amoxicilina = Producto(0L, "Amoxicilina 500 mg", 12.5, 30)

    @Test
    fun unFalloTransitorioSumaIntentoYNoMarcaFallida() = runTest {
        val servidor = PharmaSoftFalso().apply { fallarAltasCon = HttpStatusCode.ServiceUnavailable }
        val local = ProductoLocal(baseEnMemoria())
        val repo = repositorioOfflineCon(servidor, local)
        val pendiente = local.guardarPendiente(amoxicilina)

        val resumen = repo.sincronizar()

        val fila = local.porEnviar(MAX_INTENTOS_ENVIO).single()
        assertEquals(pendiente.id, fila.producto.id)
        assertEquals(1, fila.intentos)
        assertEquals(ResumenSync(enviadas = 0, fallidas = 0, pendientes = 1), resumen)
        assertEquals(EstadoEnvio(pendientes = 1), repo.observarEstadoEnvio().first())
    }

    @Test
    fun unFalloPermanenteMarcaLaFilaYNoLaReintenta() = runTest {
        val servidor = PharmaSoftFalso().apply { rechazarAltas = true }
        val local = ProductoLocal(baseEnMemoria())
        val repo = repositorioOfflineCon(servidor, local)
        val pendiente = local.guardarPendiente(amoxicilina)

        val primera = repo.sincronizar()
        repo.sincronizar()

        assertEquals(1, primera.fallidas)
        assertEquals(1, servidor.altasIntentadas)              // la segunda sincronización no la reenvió
        val fallida = repo.observarEstadoEnvio().first().fallidas.single()
        assertEquals("Amoxicilina 500 mg", fallida.nombre)
        assertTrue(fallida.motivo.startsWith("409"), "Motivo: ${fallida.motivo}")
        assertEquals(listOf(pendiente.id), local.pendientes().map { it.id })   // no se perdió
    }

    @Test
    fun laEsperaCreceYAlQuintoIntentoLaFilaQuedaFallida() = runTest {
        val servidor = PharmaSoftFalso().apply { fallarAltasCon = HttpStatusCode.ServiceUnavailable }
        val local = ProductoLocal(baseEnMemoria())
        val esperas = mutableListOf<Long>()
        val repo = repositorioOfflineCon(servidor, local, esperar = { esperas += it })
        local.guardarPendiente(amoxicilina)

        repeat(MAX_INTENTOS_ENVIO + 1) { repo.sincronizar() }

        assertEquals(listOf(1_000L, 2_000L, 4_000L, 8_000L), esperas)   // tras el 5.º no espera: se rinde
        assertEquals(MAX_INTENTOS_ENVIO, servidor.altasIntentadas)      // el 6.º sincronizar ya no la envía
        assertEquals(1, repo.observarEstadoEnvio().first().fallidas.size)
    }

    @Test
    fun sinRedNoCuentaComoIntento() = runTest {
        val servidor = PharmaSoftFalso().apply { hayRed = false }
        val local = ProductoLocal(baseEnMemoria())
        val repo = repositorioOfflineCon(servidor, local)
        local.guardarPendiente(amoxicilina)

        repeat(MAX_INTENTOS_ENVIO + 2) { assertFailsWith<ErrorApiException> { repo.sincronizar() } }

        assertEquals(0, local.porEnviar(MAX_INTENTOS_ENVIO).single().intentos)   // el modo avión no la castiga
    }

    @Test
    fun reintentarFallidosLosVuelveAEnviar() = runTest {
        val servidor = PharmaSoftFalso().apply { rechazarAltas = true }
        val local = ProductoLocal(baseEnMemoria())
        val repo = repositorioOfflineCon(servidor, local)
        local.guardarPendiente(amoxicilina)
        repo.sincronizar()

        servidor.rechazarAltas = false
        repo.reintentarFallidos()
        val resumen = repo.sincronizar()

        assertEquals(1, resumen.enviadas)
        assertEquals(EstadoEnvio(), repo.observarEstadoEnvio().first())
        assertEquals(listOf("Amoxicilina 500 mg"), servidor.productos.map { it.nombre })
    }

    @Test
    fun laEsperaSeDuplicaHastaUnTope() {
        assertEquals(
            listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 16_000L),
            (0..5).map { esperaParaIntento(it) }
        )
    }
}
