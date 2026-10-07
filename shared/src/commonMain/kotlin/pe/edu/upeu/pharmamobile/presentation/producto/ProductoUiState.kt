package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto

/**
 * [fase] describe la pantalla completa; [operacion], la acción que el usuario
 * lanzó (crear, actualizar, eliminar). Separarlas evita que guardar un producto
 * vuelva la lista a Cargando.
 */
data class ProductoUiState(
    val fase: Fase = Fase.Cargando,
    val formulario: FormularioProducto = FormularioProducto(),
    val operacion: Operacion = Operacion.Inactiva,
    val mensajeExito: String? = null
) {
    sealed interface Fase {
        data object Cargando : Fase
        data object SinProductos : Fase
        data class ConProductos(val productos: List<Producto>) : Fase
        data class Error(val mensaje: String) : Fase
    }

    sealed interface Operacion {
        data object Inactiva : Operacion
        data class EnCurso(val tipo: Tipo) : Operacion
        data class Fallida(val mensaje: String) : Operacion

        enum class Tipo { Crear, Actualizar, Eliminar }
    }

    /** Atajo para Inicio y las pestañas: la lista solo existe en ConProductos. */
    val productos: List<Producto>
        get() = (fase as? Fase.ConProductos)?.productos.orEmpty()

    val guardando: Boolean
        get() = operacion is Operacion.EnCurso && operacion.tipo != Operacion.Tipo.Eliminar

    val eliminando: Boolean
        get() = operacion == Operacion.EnCurso(Operacion.Tipo.Eliminar)
}

/**
 * Formulario de alta y edición. [id] nulo = producto nuevo. Los errores por
 * campo vienen de la validación local o del 400 de PharmaSoft (validationErrors).
 */
data class FormularioProducto(
    val abierto: Boolean = false,
    val id: Long? = null,
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val activo: Boolean = true,
    val nombreError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null
) {
    val esEdicion: Boolean get() = id != null
}
