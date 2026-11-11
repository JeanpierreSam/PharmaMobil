package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.domain.error.EnvioRechazadoException
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResumenSync
import pe.edu.upeu.pharmamobile.domain.port.Telemetria
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/** Reemplaza a ListarProductosUseCase: ya no se pide la lista, se observa. */
class ObservarProductosUseCase(private val repository: ProductoRepository) {
    operator fun invoke(): Flow<List<Producto>> = repository.observar()
}

/**
 * Subir lo pendiente y bajar del servidor. El fallo (sin red) llega como Result.
 * Sesión 13: cada sincronización deja un evento; lo que de verdad falló, un error
 * con contexto técnico. Quedarse sin red o con la sesión vencida no es un error.
 */
class SincronizarProductosUseCase(
    private val repository: ProductoRepository,
    private val telemetria: Telemetria
) {
    suspend operator fun invoke(): Result<ResumenSync> =
        resultadoDe { repository.sincronizar() }
            .onSuccess { resumen ->
                telemetria.registrarEvento(
                    "sincronizacion",
                    mapOf(
                        "enviadas" to resumen.enviadas.toString(),
                        "fallidas" to resumen.fallidas.toString(),
                        "pendientes" to resumen.pendientes.toString()
                    )
                )
                if (resumen.fallidas > 0) {
                    telemetria.registrarError(
                        EnvioRechazadoException(resumen.fallidas),
                        mapOf("pantalla" to "productos", "operacion" to "sincronizar")
                    )
                }
            }
            .onFailure { fallo ->
                if (!fallo.esEsperable()) {
                    telemetria.registrarError(
                        fallo,
                        mapOf("pantalla" to "productos", "operacion" to "sincronizar", "error" to fallo.nombreTecnico())
                    )
                }
            }
}

private fun Throwable.esEsperable(): Boolean =
    when ((this as? ErrorApiException)?.error) {
        ErrorApi.SinConexion, ErrorApi.TiempoAgotado, is ErrorApi.NoAutenticado -> true
        else -> false
    }

private fun Throwable.nombreTecnico(): String =
    (this as? ErrorApiException)?.error?.let { it::class.simpleName } ?: this::class.simpleName ?: "Desconocido"

/** Para mostrar «Actualizado hace 2 horas» (actividad autónoma, producto 3). */
class ObservarUltimaSincronizacionUseCase(private val repository: ProductoRepository) {
    operator fun invoke(): Flow<Long?> = repository.observarUltimaSincronizacion()
}
