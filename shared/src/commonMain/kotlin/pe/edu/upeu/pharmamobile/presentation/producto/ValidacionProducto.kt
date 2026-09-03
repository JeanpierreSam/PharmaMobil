package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto

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
 * Resultado de intentar registrar un producto a partir del formulario.
 */
sealed interface ResultadoRegistro {
    /** El formulario es valido y se pudo construir el objeto del dominio. */
    data class Exitoso(val producto: Producto) : ResultadoRegistro

    /** Alguna regla de negocio no se cumplio; [mensaje] explica cual. */
    data class Invalido(val mensaje: String) : ResultadoRegistro
}

/**
 * Valida los datos crudos del formulario y, solo si todas las reglas se
 * cumplen, instancia un [Producto].
 *
 * La evaluacion es estricta y en orden: nombre, conversion de precio,
 * rango de precio, conversion de stock, rango de stock. El objeto del
 * dominio se construye unicamente en el bloque `else`, porque [Producto]
 * valida en su `init` y lanzaria una excepcion con datos invalidos.
 *
 * Las conversiones usan `toDoubleOrNull()` y `toIntOrNull()` para que una
 * entrada no numerica devuelva `null` en lugar de romper la aplicacion.
 */
fun validarYCrearProducto(
    id: Long,
    nombre: String,
    precio: String,
    stock: String,
    activo: Boolean = true
): ResultadoRegistro {
    val precioIngresado = precio.toDoubleOrNull()
    val stockIngresado = stock.toIntOrNull()

    return when {
        nombre.isBlank() ->
            ResultadoRegistro.Invalido(MensajesProducto.NOMBRE_OBLIGATORIO)

        precioIngresado == null ->
            ResultadoRegistro.Invalido(MensajesProducto.PRECIO_NO_NUMERICO)

        precioIngresado <= 0.0 ->
            ResultadoRegistro.Invalido(MensajesProducto.PRECIO_NO_POSITIVO)

        stockIngresado == null ->
            ResultadoRegistro.Invalido(MensajesProducto.STOCK_NO_ENTERO)

        stockIngresado < 0 ->
            ResultadoRegistro.Invalido(MensajesProducto.STOCK_NEGATIVO)

        else -> ResultadoRegistro.Exitoso(
            Producto(
                id = id,
                nombre = nombre.trim(),
                precio = precioIngresado,
                stock = stockIngresado,
                activo = activo
            )
        )
    }
}

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
