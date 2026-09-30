package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ErrorRegistroProducto
import pe.edu.upeu.pharmamobile.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUiState.Fase
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUiState.Operacion

class ProductoViewModel(
    private val listarProductos: ListarProductosUseCase,
    private val obtenerProducto: ObtenerProductoUseCase,
    private val registrarProducto: RegistrarProductoUseCase,
    private val actualizarProducto: ActualizarProductoUseCase,
    private val eliminarProducto: EliminarProductoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init { cargarProductos() }

    fun cargarProductos() {
        viewModelScope.launch {
            // Si ya hay lista en pantalla se refresca sin volver a Cargando (no parpadea).
            if (_uiState.value.fase !is Fase.ConProductos) {
                _uiState.update { it.copy(fase = Fase.Cargando) }
            }
            val fase = listarProductos().fold(
                onSuccess = { lista -> if (lista.isEmpty()) Fase.SinProductos else Fase.ConProductos(lista) },
                onFailure = { fallo -> Fase.Error(mensajeDeFallo(fallo)) }
            )
            _uiState.update { it.copy(fase = fase) }
        }
    }

    // --- Formulario -------------------------------------------------------

    fun nuevoProducto() {
        _uiState.update { it.copy(formulario = FormularioProducto(abierto = true), mensajeExito = null) }
    }

    /** Pide el producto al servidor (GET /productos/{id}) y precarga el formulario. */
    fun editar(id: Long) {
        viewModelScope.launch {
            obtenerProducto(id)
                .onSuccess { p ->
                    _uiState.update {
                        it.copy(
                            formulario = FormularioProducto(
                                abierto = true,
                                id = p.id,
                                nombre = p.nombre,
                                precio = p.precio.toString(),
                                stock = p.stock.toString(),
                                activo = p.activo
                            ),
                            mensajeExito = null
                        )
                    }
                }
                .onFailure { fallo -> manejarFallo(fallo) }
        }
    }

    fun cerrarFormulario() {
        _uiState.update { it.copy(formulario = FormularioProducto(), operacion = Operacion.Inactiva) }
    }

    fun onNombreChange(valor: String) = actualizarFormulario { it.copy(nombre = valor, nombreError = null) }
    fun onPrecioChange(valor: String) = actualizarFormulario { it.copy(precio = valor, precioError = null) }
    fun onStockChange(valor: String) = actualizarFormulario { it.copy(stock = valor, stockError = null) }
    fun onActivoChange(valor: Boolean) = actualizarFormulario { it.copy(activo = valor) }

    private fun actualizarFormulario(cambio: (FormularioProducto) -> FormularioProducto) {
        _uiState.update { it.copy(formulario = cambio(it.formulario)) }
    }

    // --- Operaciones ------------------------------------------------------

    /** Crea o actualiza según el formulario tenga id. */
    fun guardar() {
        val estado = _uiState.value
        if (estado.operacion is Operacion.EnCurso) return
        val f = estado.formulario
        val tipo = if (f.esEdicion) Operacion.Tipo.Actualizar else Operacion.Tipo.Crear

        viewModelScope.launch {
            _uiState.update { it.copy(operacion = Operacion.EnCurso(tipo), mensajeExito = null) }
            val resultado = if (f.id != null) {
                actualizarProducto(f.id, f.nombre, f.precio, f.stock, f.activo)
            } else {
                registrarProducto(f.nombre, f.precio, f.stock, f.activo)
            }
            resultado
                .onSuccess { producto ->
                    _uiState.update {
                        it.copy(
                            formulario = FormularioProducto(),
                            operacion = Operacion.Inactiva,
                            mensajeExito = if (tipo == Operacion.Tipo.Crear) {
                                "Producto \"${producto.nombre}\" registrado"
                            } else {
                                "Producto \"${producto.nombre}\" actualizado"
                            }
                        )
                    }
                    cargarProductos()
                }
                .onFailure { fallo -> manejarFallo(fallo) }
        }
    }

    fun eliminar(id: Long) {
        if (_uiState.value.operacion is Operacion.EnCurso) return
        viewModelScope.launch {
            _uiState.update { it.copy(operacion = Operacion.EnCurso(Operacion.Tipo.Eliminar), mensajeExito = null) }
            eliminarProducto(id)
                .onSuccess {
                    _uiState.update { it.copy(operacion = Operacion.Inactiva, mensajeExito = "Producto dado de baja") }
                    cargarProductos()
                }
                .onFailure { fallo -> manejarFallo(fallo) }
        }
    }

    /** La pantalla lo llama después de mostrar el Snackbar. */
    fun mensajeMostrado() {
        _uiState.update {
            it.copy(
                mensajeExito = null,
                operacion = if (it.operacion is Operacion.Fallida) Operacion.Inactiva else it.operacion
            )
        }
    }

    // --- Errores ----------------------------------------------------------

    private fun manejarFallo(fallo: Throwable) {
        val errorApi = (fallo as? ErrorApiException)?.error
        val errorLocal = errorLocalDe(fallo)
        when {
            // 400 de PharmaSoft: cada mensaje va debajo de su campo.
            errorApi is ErrorApi.Validacion -> _uiState.update {
                it.copy(
                    operacion = Operacion.Inactiva,
                    formulario = it.formulario.copy(
                        nombreError = errorApi.porCampo["nombre"],
                        precioError = errorApi.porCampo["precio"],
                        stockError = errorApi.porCampo["stock"]
                    )
                )
            }
            // Validación local previa al envío: mismo lugar, sin llamar al servidor.
            errorLocal != null -> _uiState.update {
                it.copy(operacion = Operacion.Inactiva, formulario = conErrorLocal(it.formulario, errorLocal))
            }
            else -> _uiState.update { it.copy(operacion = Operacion.Fallida(mensajeDeFallo(fallo))) }
        }
    }

    private fun mensajeDeFallo(fallo: Throwable): String =
        (fallo as? ErrorApiException)?.error?.let(::mensajeDe) ?: fallo.message ?: "Error desconocido"

    private fun errorLocalDe(fallo: Throwable): ErrorRegistroProducto? {
        if (fallo !is IllegalArgumentException) return null
        return listOf(
            ErrorRegistroProducto.NombreObligatorio,
            ErrorRegistroProducto.PrecioNoNumerico,
            ErrorRegistroProducto.PrecioNoPositivo,
            ErrorRegistroProducto.StockNoEntero,
            ErrorRegistroProducto.StockNegativo
        ).firstOrNull { it::class.simpleName == fallo.message }
    }

    private fun conErrorLocal(f: FormularioProducto, error: ErrorRegistroProducto): FormularioProducto =
        when (error) {
            ErrorRegistroProducto.NombreObligatorio -> f.copy(nombreError = MensajesProducto.NOMBRE_OBLIGATORIO)
            ErrorRegistroProducto.PrecioNoNumerico -> f.copy(precioError = MensajesProducto.PRECIO_NO_NUMERICO)
            ErrorRegistroProducto.PrecioNoPositivo -> f.copy(precioError = MensajesProducto.PRECIO_NO_POSITIVO)
            ErrorRegistroProducto.StockNoEntero -> f.copy(stockError = MensajesProducto.STOCK_NO_ENTERO)
            ErrorRegistroProducto.StockNegativo -> f.copy(stockError = MensajesProducto.STOCK_NEGATIVO)
        }
}
