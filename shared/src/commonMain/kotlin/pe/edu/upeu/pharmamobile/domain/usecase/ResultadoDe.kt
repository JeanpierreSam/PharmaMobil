package pe.edu.upeu.pharmamobile.domain.usecase

import kotlin.coroutines.cancellation.CancellationException

/**
 * Como runCatching, pero relanza la CancellationException: si el usuario
 * cierra la pantalla, la corrutina debe cancelarse y no convertirse en un
 * error que la interfaz intente mostrar.
 */
internal suspend fun <T> resultadoDe(bloque: suspend () -> T): Result<T> =
    try {
        Result.success(bloque())
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (fallo: Throwable) {
        Result.failure(fallo)
    }
