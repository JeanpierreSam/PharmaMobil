package pe.edu.upeu.pharmamobile.domain.model

/** Resultado de enviar UNA fila pendiente. El dominio no conoce códigos HTTP. */
sealed interface ResultadoEnvio {
    data class Exito(val idServidor: Long) : ResultadoEnvio
    /** Puede funcionar más tarde: tiempo agotado, servidor caído. Se reintenta con espera. */
    data class Transitorio(val motivo: String) : ResultadoEnvio
    /** Reintentar daría lo mismo: validación, conflicto, recurso inexistente, sin permiso. */
    data class Permanente(val motivo: String) : ResultadoEnvio
}

/** Lo que devuelve una sincronización completa. */
data class ResumenSync(
    val enviadas: Int,
    val fallidas: Int,
    val pendientes: Int
)
