package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

sealed interface ErrorRegistroProducto {
    data object NombreObligatorio : ErrorRegistroProducto
    data object PrecioNoNumerico : ErrorRegistroProducto
    data object PrecioNoPositivo : ErrorRegistroProducto
    data object StockNoEntero : ErrorRegistroProducto
    data object StockNegativo : ErrorRegistroProducto
}

class RegistrarProductoUseCase(
    private val repository: ProductoRepository
) {
    suspend operator fun invoke(nombre: String, precio: String, stock: String, activo: Boolean): Result<Producto> =
        construirProducto(id = 0L, nombre, precio, stock, activo).fold(
            onSuccess = { producto -> resultadoDe { repository.registrar(producto) } },
            onFailure = { Result.failure(it) }
        )
}

/**
 * Validación local previa al envío: solo lo que el modelo Producto no admite
 * (vacío, no numérico, precio <= 0, stock negativo). Reglas como la longitud
 * del nombre las decide PharmaSoft y llegan como ErrorApi.Validacion.
 */
internal fun construirProducto(
    id: Long,
    nombre: String,
    precio: String,
    stock: String,
    activo: Boolean
): Result<Producto> {
    val precioValor = precio.toDoubleOrNull()
    val stockValor = stock.toIntOrNull()

    val error = when {
        nombre.isBlank() -> ErrorRegistroProducto.NombreObligatorio
        precioValor == null -> ErrorRegistroProducto.PrecioNoNumerico
        precioValor <= 0.0 -> ErrorRegistroProducto.PrecioNoPositivo
        stockValor == null -> ErrorRegistroProducto.StockNoEntero
        stockValor < 0 -> ErrorRegistroProducto.StockNegativo
        else -> null
    }

    if (error != null) return Result.failure(IllegalArgumentException(error::class.simpleName))

    return Result.success(
        Producto(id = id, nombre = nombre.trim(), precio = precioValor!!, stock = stockValor!!, activo = activo)
    )
}
