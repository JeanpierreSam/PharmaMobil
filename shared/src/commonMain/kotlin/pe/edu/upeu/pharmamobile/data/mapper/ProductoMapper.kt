package pe.edu.upeu.pharmamobile.data.mapper

import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobile.domain.model.Producto

// El estado de PharmaSoft es el mismo concepto que Producto.activo: la baja
// lógica (DELETE) lo pone en false y el producto pasa a la pestaña Inactivos.
fun ProductoResponseDto.toDomain(): Producto = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock,
    activo = estado
)

// El dominio no conoce categorías; el backend la exige. Se decide aquí, no en la UI.
fun Producto.toRequest(categoriaId: Long): ProductoRequestDto = ProductoRequestDto(
    nombre = nombre,
    precio = precio,
    stock = stock,
    estado = activo,
    categoriaId = categoriaId
)
