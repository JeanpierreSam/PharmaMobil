package pe.edu.upeu.pharmamobile.presentation.avisos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso
import pe.edu.upeu.pharmamobile.domain.platform.Notificador

/** Tres estados del permiso → tres respuestas: continuar, explicar y pedir, o llevar a Ajustes. */
class AvisosStockViewModel(
    private val gestor: GestorPermisos,
    private val notificador: Notificador
) : ViewModel() {

    /** null mientras se consulta por primera vez. */
    private val _estado = MutableStateFlow<EstadoPermiso?>(null)
    val estado: StateFlow<EstadoPermiso?> = _estado.asStateFlow()

    init {
        actualizarEstado()
    }

    /** Consulta SIN pedir: abrir la pestaña nunca muestra el diálogo del sistema. */
    fun actualizarEstado() {
        viewModelScope.launch { _estado.value = gestor.estado(Permiso.NOTIFICACIONES) }
    }

    /** Solo cuando el usuario pulsa «Activar avisos». */
    fun activar() {
        viewModelScope.launch { _estado.value = gestor.solicitar(Permiso.NOTIFICACIONES) }
    }

    fun abrirAjustes() = gestor.abrirAjustes()

    fun avisarAhora(productosBajoStock: Int) {
        if (_estado.value != EstadoPermiso.Concedido) return
        notificador.avisar("Stock bajo en PharmaMobil", textoAvisoStock(productosBajoStock))
    }
}

fun textoAvisoStock(productos: Int): String =
    if (productos == 1) "1 producto tiene ${Producto.STOCK_MINIMO} unidades o menos."
    else "$productos productos tienen ${Producto.STOCK_MINIMO} unidades o menos."
