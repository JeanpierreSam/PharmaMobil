package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable

/** Cuerpo de POST y PUT: PharmaSoft exige los cinco campos (PUT reemplaza, no hay PATCH). */
@Serializable
data class ProductoRequestDto(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long
)

/** fechaCreacion y fechaModificacion no se declaran: ignoreUnknownKeys las descarta. */
@Serializable
data class ProductoResponseDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long? = null,
    val categoriaNombre: String? = null
)
