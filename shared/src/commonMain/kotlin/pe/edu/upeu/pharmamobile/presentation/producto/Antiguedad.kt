package pe.edu.upeu.pharmamobile.presentation.producto

/**
 * «Actualizado hace 2 horas». Función pura (recibe la hora actual) para poder
 * probarla sin esperar ni cambiar el reloj del dispositivo.
 */
fun textoAntiguedad(ultimaSincronizacion: Long?, ahora: Long): String {
    if (ultimaSincronizacion == null) return "Nunca sincronizado: los datos pueden estar incompletos"
    val minutos = ((ahora - ultimaSincronizacion) / 60_000L).coerceAtLeast(0L)
    return when {
        minutos < 1 -> "Actualizado hace un momento"
        minutos < 60 -> "Actualizado hace ${cantidad(minutos, "minuto")}"
        minutos < 60 * 24 -> "Actualizado hace ${cantidad(minutos / 60, "hora")}"
        else -> "Actualizado hace ${cantidad(minutos / (60 * 24), "día")}"
    }
}

private fun cantidad(n: Long, unidad: String): String = if (n == 1L) "1 $unidad" else "$n ${unidad}s"
