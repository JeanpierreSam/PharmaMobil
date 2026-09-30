package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/** En PharmaSoft es una baja lógica: el producto queda inactivo, no se borra. */
class EliminarProductoUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(id: Long): Result<Unit> = resultadoDe { repository.eliminar(id) }
}
