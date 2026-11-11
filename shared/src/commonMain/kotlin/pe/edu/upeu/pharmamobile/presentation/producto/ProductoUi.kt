package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.platform.formatearSoles

/**
 * Producto listo para mostrarse: el precio ya viene como texto en soles.
 * El formato se aplica aquí, en el mapeo de presentación, y no en el dominio
 * ni dentro del composable.
 */
data class ProductoUi(
    val id: Long,
    val nombre: String,
    val precio: String,
    val stock: Int,
    val activo: Boolean,
    val requiereReposicion: Boolean,
    /** Sesión 11: creado sin conexión, PharmaSoft todavía no lo tiene. */
    val pendiente: Boolean
)

fun Producto.toUi() = ProductoUi(
    id = id,
    nombre = nombre,
    precio = formatearSoles(precio),
    stock = stock,
    activo = activo,
    requiereReposicion = requiereReposicion,
    // Los ids negativos los asigna la base local: el servidor nunca los usa.
    pendiente = id < 0
)
