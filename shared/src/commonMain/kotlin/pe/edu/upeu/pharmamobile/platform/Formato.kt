package pe.edu.upeu.pharmamobile.platform

/**
 * Precio en soles con el formato de moneda de Perú. Cada plataforma usa su
 * propia API de localización (NumberFormat en Android, NSNumberFormatter en iOS),
 * por eso el resultado puede diferir en espacios o separadores.
 */
expect fun formatearSoles(valor: Double): String
