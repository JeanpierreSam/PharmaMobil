package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/** Mismas reglas locales que el registro; el id viaja en la ruta del PUT. */
class ActualizarProductoUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(
        id: Long,
        nombre: String,
        precio: String,
        stock: String,
        activo: Boolean
    ): Result<Producto> =
        construirProducto(id, nombre, precio, stock, activo).fold(
            onSuccess = { producto -> resultadoDe { repository.actualizar(producto) } },
            onFailure = { Result.failure(it) }
        )
}
