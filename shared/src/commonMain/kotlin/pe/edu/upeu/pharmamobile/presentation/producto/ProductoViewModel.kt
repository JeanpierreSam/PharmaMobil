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
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ErrorRegistroProducto
import pe.edu.upeu.pharmamobile.domain.usecase.ObservarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.SincronizarProductosUseCase
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUiState.Fase
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUiState.Operacion

class ProductoViewModel(
    private val observarProductos: ObservarProductosUseCase,
    private val sincronizarProductos: SincronizarProductosUseCase,
    private val obtenerProducto: ObtenerProductoUseCase,
    private val registrarProducto: RegistrarProductoUseCase,
    private val actualizarProducto: ActualizarProductoUseCase,
    private val eliminarProducto: EliminarProductoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        // Se suscribe UNA vez a la base: cada escritura en la tabla llega sola aquí.
        viewModelScope.launch {
            observarProductos().collect { productos ->
                _uiState.update { it.copy(fase = faseDe(productos)) }
            }
        }
        sincronizar()
    }

    /** Desde el init y desde el botón de la barra. No devuelve datos: los trae el Flow. */
    fun sincronizar() {
        if (_uiState.value.sincronizando) return
        viewModelScope.launch {
            _uiState.update { it.copy(sincronizando = true) }
            val resultado = sincronizarProductos()
            _uiState.update {
                it.copy(
                    sincronizando = false,
                    avisoRed = resultado.exceptionOrNull()?.let(::avisoDeSincronizacion)
                )
            }
        }
    }

    private fun faseDe(productos: List<Producto>): Fase =
        if (productos.isEmpty()) Fase.SinProductos else Fase.ConProductos(productos)

    private fun avisoDeSincronizacion(fallo: Throwable): String =
        when ((fallo as? ErrorApiException)?.error) {
            ErrorApi.SinConexion, ErrorApi.TiempoAgotado -> "Sin conexión: mostrando datos guardados"
            else -> "No se pudo sincronizar: ${mensajeDeFallo(fallo)}"
        }

    // --- Formulario -------------------------------------------------------

    fun nuevoProducto() {
        _uiState.update { it.copy(formulario = FormularioProducto(abierto = true), mensajeExito = null) }
    }

    /** Lee el producto de la base local y precarga el formulario (funciona sin red). */
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

    // Al corregir un campo se descarta el error de la operación anterior (409, 404...).
    private fun actualizarFormulario(cambio: (FormularioProducto) -> FormularioProducto) {
        _uiState.update {
            it.copy(
                formulario = cambio(it.formulario),
                operacion = if (it.operacion is Operacion.Fallida) Operacion.Inactiva else it.operacion
            )
        }
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
                            mensajeExito = when {
                                // id negativo: quedó en la base local, PharmaSoft aún no lo tiene
                                producto.id < 0 -> "Sin conexión: \"${producto.nombre}\" quedó pendiente de envío"
                                tipo == Operacion.Tipo.Crear -> "Producto \"${producto.nombre}\" registrado"
                                else -> "Producto \"${producto.nombre}\" actualizado"
                            }
                        )
                    }
                    // Ya no se recarga la lista: se escribió en la tabla y el Flow emitió.
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
