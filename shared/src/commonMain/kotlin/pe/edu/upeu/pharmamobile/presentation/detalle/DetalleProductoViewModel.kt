package pe.edu.upeu.pharmamobile.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.comoTextoParaCompartir
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUi
import pe.edu.upeu.pharmamobile.presentation.producto.mensajeDe
import pe.edu.upeu.pharmamobile.presentation.producto.toUi

/**
 * Detalle de un producto (GET /productos/{id}) y la acción Compartir.
 * Recibe el [Compartidor] por inyección: no sabe si detrás hay un Intent
 * o un UIActivityViewController, y en las pruebas se sustituye por un doble.
 */
class DetalleProductoViewModel(
    private val obtenerProducto: ObtenerProductoUseCase,
    private val compartidor: Compartidor
) : ViewModel() {

    private val _estado = MutableStateFlow<DetalleUiState>(DetalleUiState.Cerrado)
    val estado: StateFlow<DetalleUiState> = _estado.asStateFlow()

    fun abrir(id: Long) {
        _estado.value = DetalleUiState.Cargando
        viewModelScope.launch {
            _estado.value = obtenerProducto(id).fold(
                onSuccess = { DetalleUiState.Listo(it, it.toUi()) },
                onFailure = { fallo ->
                    DetalleUiState.Error(
                        (fallo as? ErrorApiException)?.error?.let(::mensajeDe)
                            ?: fallo.message ?: "Error desconocido"
                    )
                }
            )
        }
    }

    fun cerrar() {
        _estado.value = DetalleUiState.Cerrado
    }

    fun compartir(producto: Producto) {
        compartidor.compartir(producto.comoTextoParaCompartir())
    }
}

sealed interface DetalleUiState {
    data object Cerrado : DetalleUiState
    data object Cargando : DetalleUiState
    /** [producto] alimenta la acción Compartir; [ui] es lo que se dibuja. */
    data class Listo(val producto: Producto, val ui: ProductoUi) : DetalleUiState
    data class Error(val mensaje: String) : DetalleUiState
}
