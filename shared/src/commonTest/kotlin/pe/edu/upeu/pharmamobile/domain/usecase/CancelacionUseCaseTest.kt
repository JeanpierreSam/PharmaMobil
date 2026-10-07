package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.repository.FakeProductoRepository
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Escenario 8 de la bitácora (cancelación): si la corrutina se cancela a mitad
 * de una operación, resultadoDe relanza la CancellationException en lugar de
 * convertirla en un Result.failure que la pantalla intentaría mostrar.
 * Con runCatching esta prueba falla: el resultado deja de ser null.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CancelacionUseCaseTest {

    @Test
    fun cancelarDuranteLaEliminacion_noProduceResultadoNiError() = runTest {
        val repo = FakeProductoRepository(retardoMs = 1_000)
        val eliminar = EliminarProductoUseCase(repo)
        var resultado: Result<Unit>? = null

        val trabajo = launch { resultado = eliminar(1L) }
        advanceTimeBy(500)
        trabajo.cancel()
        advanceUntilIdle()

        assertTrue(trabajo.isCancelled)
        assertNull(resultado)
    }
}
