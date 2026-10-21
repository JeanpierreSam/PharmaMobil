package pe.edu.upeu.pharmamobile.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesionException
import pe.edu.upeu.pharmamobile.domain.model.EstadoSesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobile.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RestaurarSesionUseCase

class SesionViewModel(
    authRepository: AuthRepository,
    private val iniciarSesion: IniciarSesionUseCase,
    private val cerrarSesion: CerrarSesionUseCase,
    private val restaurarSesion: RestaurarSesionUseCase,
    val esDepuracion: Boolean
) : ViewModel() {

    val estado: StateFlow<EstadoSesion> = authRepository.estado

    private val _uiState = MutableStateFlow(SesionUiState())
    val uiState: StateFlow<SesionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { restaurarSesion() }
        // Cada vez que la sesión termina (salir, expirado, deshabilitado) el formulario
        // se reinicia: no queda el usuario anterior ni su clave (efecto 3 de la GAA).
        viewModelScope.launch {
            estado.collect { e ->
                if (e is EstadoSesion.Inactiva) _uiState.value = SesionUiState(aviso = e.aviso)
            }
        }
    }

    fun onUsuarioChange(valor: String) = _uiState.update { it.copy(usuario = valor, error = null) }
    fun onClaveChange(valor: String) = _uiState.update { it.copy(clave = valor, error = null) }

    fun ingresar() {
        val actual = _uiState.value
        if (actual.cargando) return
        if (!actual.puedeIngresar) {
            _uiState.update { it.copy(error = "Ingrese usuario y contraseña") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null, aviso = null) }
            iniciarSesion(Credenciales(actual.usuario, actual.clave))
                .onSuccess { _uiState.value = SesionUiState() }   // la clave no queda en memoria
                .onFailure { fallo ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            clave = "",
                            error = mensajeSesion((fallo as? ErrorSesionException)?.error)
                        )
                    }
                }
        }
    }

    fun salir() {
        viewModelScope.launch { cerrarSesion() }
    }
}

fun mensajeSesion(error: ErrorSesion?): String = when (error) {
    ErrorSesion.CredencialesInvalidas -> "Usuario o contraseña incorrectos."
    is ErrorSesion.SesionExpirada -> error.mensaje
    is ErrorSesion.NoAutenticado -> error.mensaje
    ErrorSesion.SinPermiso -> "No tiene permisos para realizar esta operación."
    // Sesión 11: el único paso que de verdad exige red es el primer inicio de sesión.
    ErrorSesion.SinConexion -> "Sin conexión con el servidor. Necesitas internet para iniciar sesión; después la app funciona sin red."
    null -> "Error desconocido."
}
