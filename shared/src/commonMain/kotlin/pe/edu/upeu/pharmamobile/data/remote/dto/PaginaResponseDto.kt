package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable

/** El listado de PharmaSoft llega envuelto: los elementos van en [contenido]. */
@Serializable
data class PaginaResponseDto<T>(
    val contenido: List<T>,
    val pagina: Int,
    val tamanio: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
    val ultima: Boolean
)
