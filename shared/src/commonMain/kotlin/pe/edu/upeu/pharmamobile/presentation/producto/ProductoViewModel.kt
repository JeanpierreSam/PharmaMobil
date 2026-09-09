package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.ErrorRegistroProducto
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val repository: ProductoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init { cargarProductos() }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = ProductoUiState.Fase.Cargando) }
            runCatching { repository.listar() }
                .onSuccess { lista ->
                    _uiState.update {
                        it.copy(
                            productos = lista,
                            fase = if (lista.isEmpty()) {
                                ProductoUiState.Fase.SinProductos
                            } else {
                                ProductoUiState.Fase.ConProductos
                            }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(fase = ProductoUiState.Fase.Error(e.message ?: "Error desconocido")) }
                }
        }
    }

    fun registrar(nombre: String, precio: String, stock: String, activo: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            registrarProducto.invoke(nombre, precio, stock, activo)
                .onSuccess {
                    _uiState.update { it.copy(guardando = false, mensaje = MensajesProducto.REGISTRO_EXITOSO) }
                    cargarProductos()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(guardando = false, mensaje = mapearError(e)) }
                }
        }
    }

    private fun mapearError(e: Throwable): String = when (e.message) {
        ErrorRegistroProducto.NombreObligatorio::class.simpleName -> MensajesProducto.NOMBRE_OBLIGATORIO
        ErrorRegistroProducto.PrecioNoNumerico::class.simpleName -> MensajesProducto.PRECIO_NO_NUMERICO
        ErrorRegistroProducto.PrecioNoPositivo::class.simpleName -> MensajesProducto.PRECIO_NO_POSITIVO
        ErrorRegistroProducto.StockNoEntero::class.simpleName -> MensajesProducto.STOCK_NO_ENTERO
        ErrorRegistroProducto.StockNegativo::class.simpleName -> MensajesProducto.STOCK_NEGATIVO
        else -> "Error al registrar el producto"
    }
}
