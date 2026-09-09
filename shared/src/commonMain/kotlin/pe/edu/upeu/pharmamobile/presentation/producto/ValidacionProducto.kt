package pe.edu.upeu.pharmamobile.presentation.producto

/**
 * Mensajes de error del formulario de registro de productos.
 *
 * Se centralizan aqui para que la UI y las pruebas usen exactamente
 * el mismo texto y no se dupliquen literales por el codigo.
 */
object MensajesProducto {
    const val NOMBRE_OBLIGATORIO = "El nombre es obligatorio."
    const val PRECIO_NO_NUMERICO = "Ingrese un precio numérico."
    const val PRECIO_NO_POSITIVO = "El precio debe ser mayor que cero."
    const val STOCK_NO_ENTERO = "Ingrese un stock entero."
    const val STOCK_NEGATIVO = "El stock no puede ser negativo."
    const val REGISTRO_EXITOSO = "Producto registrado correctamente."
}

/**
 * Helpers de resaltado en tiempo real para [ProductoScreen]. La decisión de
 * si el formulario se puede enviar vive en `RegistrarProductoUseCase`
 * (domain/usecase): estas funciones solo controlan el estado visual del
 * campo mientras el usuario escribe.
 */

/** El nombre no puede estar vacio ni contener solo espacios. */
fun nombreEsInvalido(nombre: String): Boolean = nombre.isBlank()

/** El precio debe ser numerico y estrictamente mayor a cero. */
fun precioEsInvalido(precio: String): Boolean {
    val valor = precio.toDoubleOrNull()
    return valor == null || valor <= 0.0
}

/** El stock debe ser un entero mayor o igual a cero. */
fun stockEsInvalido(stock: String): Boolean {
    val valor = stock.toIntOrNull()
    return valor == null || valor < 0
}
