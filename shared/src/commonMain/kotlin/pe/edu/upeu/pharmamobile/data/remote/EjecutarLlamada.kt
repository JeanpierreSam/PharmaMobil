package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException

suspend fun <T> ejecutarLlamada(bloque: suspend () -> T): Result<T> =
    try {
        Result.success(bloque())
    } catch (cancelacion: CancellationException) {
        throw cancelacion                         // nunca atraparla: cancela la corrutina
    } catch (e: ClientRequestException) {
        Result.failure(ErrorApiException(traducirCliente(e)))
    } catch (e: ServerResponseException) {
        Result.failure(ErrorApiException(ErrorApi.Servidor))
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ErrorApiException(ErrorApi.TiempoAgotado))
    } catch (e: IOException) {
        Result.failure(ErrorApiException(ErrorApi.SinConexion))
    }

private fun traducirCliente(e: ClientRequestException): ErrorApi =
    when (e.response.status.value) {
        404 -> ErrorApi.NoEncontrado
        else -> ErrorApi.Servidor          // la S8 agrega 400 -> Validacion y 409 -> Conflicto
    }
