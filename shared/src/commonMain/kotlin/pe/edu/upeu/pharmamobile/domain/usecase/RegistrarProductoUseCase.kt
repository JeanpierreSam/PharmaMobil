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
    suspend fun invoke(nombre: String, precio: String, stock: String, activo: Boolean): Result<Producto> {
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

        return runCatching {
            repository.registrar(nombre.trim(), precioValor!!, stockValor!!, activo)
        }
    }
}
