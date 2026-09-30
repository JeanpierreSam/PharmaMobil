package pe.edu.upeu.pharmamobile.data.mapper

import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobile.domain.model.Producto

// La API de práctica no expone inventario ni estado. PharmaSoft sí: se reemplaza en la S8.
private const val STOCK_POR_DEFECTO = 10

fun ProductoDto.toDomain(): Producto = Producto(
    id = id.toLong(),          // el dominio usa Long, la API Int
    nombre = title,
    precio = price,
    stock = STOCK_POR_DEFECTO,
    activo = true
)
