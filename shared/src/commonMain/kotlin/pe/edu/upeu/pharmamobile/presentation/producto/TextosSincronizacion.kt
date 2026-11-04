package pe.edu.upeu.pharmamobile.presentation.producto

/** «3 cambios por enviar». null si no hay nada en cola. */
fun textoPorEnviar(pendientes: Int): String? = when {
    pendientes <= 0 -> null
    pendientes == 1 -> "1 cambio por enviar"
    else -> "$pendientes cambios por enviar"
}

/** «1 cambio no se pudo enviar». null si no hay fallidas. */
fun textoFallidas(fallidas: Int): String? = when {
    fallidas <= 0 -> null
    fallidas == 1 -> "1 cambio no se pudo enviar"
    else -> "$fallidas cambios no se pudieron enviar"
}

/** «Sin conexión: mostrando datos guardados · 3 cambios por enviar». */
fun lineaEstado(aviso: String?, pendientes: Int): String? =
    listOfNotNull(aviso, textoPorEnviar(pendientes)).joinToString(" · ").ifEmpty { null }
