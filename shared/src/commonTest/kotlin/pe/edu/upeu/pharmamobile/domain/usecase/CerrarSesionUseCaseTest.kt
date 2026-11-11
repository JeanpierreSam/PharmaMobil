package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.EnvioFallido
import pe.edu.upeu.pharmamobile.domain.model.EstadoEnvio
import pe.edu.upeu.pharmamobile.domain.model.EstadoSesion
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobile.presentation.sesion.textoSalidaConPendientes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CerrarSesionUseCaseTest {

    private class FakeAuth : AuthRepository {
        override val estado: StateFlow<EstadoSesion> = MutableStateFlow(EstadoSesion.Verificando)
        var cierres = 0
            private set
        override suspend fun iniciarSesion(credenciales: Credenciales): Sesion = error("No se usa")
        override suspend fun sesionGuardada(): Sesion? = null
        override suspend fun cerrarSesion(aviso: String?) {
            cierres++
        }
    }

    @Test
    fun cerrarSesion_borraLaBaseYCierraLaSesion() = runTest {
        val repo = FakeProductoRepository(listOf(Producto(1L, "Paracetamol 500 mg", 15.5, 100)))
        val auth = FakeAuth()

        CerrarSesionUseCase(auth, repo)()

        assertTrue(repo.productos.isEmpty())
        assertEquals(1, repo.borradosLocales)
        assertEquals(1, auth.cierres)
    }

    @Test
    fun cambiosSinEnviar_cuentaPendientesYFallidas() = runTest {
        val repo = FakeProductoRepository().apply {
            envio = EstadoEnvio(pendientes = 2, fallidas = listOf(EnvioFallido(-3L, "Ibuprofeno", "409")))
        }

        assertEquals(3, ContarCambiosSinEnviarUseCase(repo)())
    }

    @Test
    fun textoSalida_singularYPlural() {
        assertTrue(textoSalidaConPendientes(1).startsWith("Tienes 1 cambio que"))
        assertTrue(textoSalidaConPendientes(3).startsWith("Tienes 3 cambios que"))
    }
}
