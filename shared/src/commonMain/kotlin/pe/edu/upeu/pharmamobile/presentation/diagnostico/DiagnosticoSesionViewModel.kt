package pe.edu.upeu.pharmamobile.presentation.diagnostico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.data.remote.DiagnosticoApi
import pe.edu.upeu.pharmamobile.data.remote.ResultadoPrueba
import pe.edu.upeu.pharmamobile.domain.model.EstadoSesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

data class DiagnosticoUiState(
    val ejecutando: Boolean = false,
    val resultados: List<ResultadoPrueba> = emptyList(),
    val nota: String? = null
)

class DiagnosticoSesionViewModel(
    private val api: DiagnosticoApi,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticoUiState())
    val uiState = _uiState.asStateFlow()

    fun probarEscenarios() = ejecutar {
        listOf(api.tokenValido(), api.tokenAlteradoEnMemoria(), api.sinCabecera(), api.prefijoMinuscula())
    }

    fun probarSinSesion() = ejecutar { listOf(api.peticionDeLaAppSinSesion()) }

    fun probarMatriz() = ejecutar {
        val usuario = (authRepository.estado.value as? EstadoSesion.Activa)?.sesion?.usuario ?: "?"
        listOf(ResultadoPrueba("Usuario de la prueba", null, usuario)) + api.matrizDePermisos()
    }

    fun alterarTokenGuardado() {
        val ok = api.alterarTokenGuardado()
        _uiState.update {
            it.copy(nota = if (ok) "Token guardado alterado. Abre el detalle de un producto." else "No hay token guardado.")
        }
    }

    private fun ejecutar(bloque: suspend () -> List<ResultadoPrueba>) {
        if (_uiState.value.ejecutando) return
        viewModelScope.launch {
            _uiState.update { it.copy(ejecutando = true, nota = null) }
            val resultados = bloque()
            resultados.forEach { println("Diagnostico: ${it.prueba} -> ${it.codigo} ${it.mensaje}") }
            _uiState.update { it.copy(ejecutando = false, resultados = resultados) }
        }
    }
}
