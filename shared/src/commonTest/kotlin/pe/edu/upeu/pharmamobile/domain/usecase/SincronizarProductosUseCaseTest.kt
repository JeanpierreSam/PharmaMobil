package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.error.EnvioRechazadoException
import pe.edu.upeu.pharmamobile.domain.model.ResumenSync
import pe.edu.upeu.pharmamobile.platform.FakeTelemetria
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SincronizarProductosUseCaseTest {

    @Test
    fun sincronizacionCorrecta_registraElEventoConLosConteos() = runTest {
        val repo = FakeProductoRepository().apply { resumen = ResumenSync(enviadas = 2, fallidas = 0, pendientes = 0) }
        val telemetria = FakeTelemetria()

        SincronizarProductosUseCase(repo, telemetria)()

        val (nombre, datos) = telemetria.eventos.single()
        assertEquals("sincronizacion", nombre)
        assertEquals("2", datos["enviadas"])
        assertTrue(telemetria.errores.isEmpty())
    }

    @Test
    fun enviosRechazados_registranUnErrorConContextoTecnico() = runTest {
        val repo = FakeProductoRepository().apply { resumen = ResumenSync(enviadas = 0, fallidas = 1, pendientes = 0) }
        val telemetria = FakeTelemetria()

        SincronizarProductosUseCase(repo, telemetria)()

        val (error, contexto) = telemetria.errores.single()
        assertIs<EnvioRechazadoException>(error)
        assertEquals(mapOf("pantalla" to "productos", "operacion" to "sincronizar"), contexto)
    }

    @Test
    fun sinRed_noEsUnErrorQueReportar() = runTest {
        val repo = FakeProductoRepository().apply { fallaAlSincronizar = ErrorApiException(ErrorApi.SinConexion) }
        val telemetria = FakeTelemetria()

        val resultado = SincronizarProductosUseCase(repo, telemetria)()

        assertTrue(resultado.isFailure)
        assertTrue(telemetria.errores.isEmpty())
    }

    @Test
    fun servidorCaido_seReportaSinDatosPersonales() = runTest {
        val repo = FakeProductoRepository().apply { fallaAlSincronizar = ErrorApiException(ErrorApi.Servidor) }
        val telemetria = FakeTelemetria()

        SincronizarProductosUseCase(repo, telemetria)()

        assertEquals(
            mapOf("pantalla" to "productos", "operacion" to "sincronizar", "error" to "Servidor"),
            telemetria.errores.single().second
        )
    }
}
