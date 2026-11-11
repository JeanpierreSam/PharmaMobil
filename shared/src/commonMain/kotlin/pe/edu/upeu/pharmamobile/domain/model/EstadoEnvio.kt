package pe.edu.upeu.pharmamobile.domain.model

/** Al quinto fallo transitorio la fila deja de reintentarse sola y se muestra como fallida. */
const val MAX_INTENTOS_ENVIO = 5

/** Producto que no se pudo enviar: lo que el usuario necesita ver para decidir. */
data class EnvioFallido(
    val id: Long,
    val nombre: String,
    val motivo: String
)

/** Lo que la barra de sincronización muestra sin consultar la red. */
data class EstadoEnvio(
    val pendientes: Int = 0,
    val fallidas: List<EnvioFallido> = emptyList()
)
