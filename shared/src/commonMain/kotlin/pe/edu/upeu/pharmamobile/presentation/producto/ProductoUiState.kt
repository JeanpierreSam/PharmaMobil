package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto

data class ProductoUiState(
    val fase: Fase = Fase.Cargando,
    val productos: List<Producto> = emptyList(),
    val guardando: Boolean = false,
    val mensaje: String? = null
) {
    sealed interface Fase {
        data object Cargando : Fase
        data object SinProductos : Fase
        data object ConProductos : Fase
        data class Error(val detalle: String) : Fase
    }
}
