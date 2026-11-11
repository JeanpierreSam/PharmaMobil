package pe.edu.upeu.pharmamobile.platform

import java.text.NumberFormat
import java.util.Locale

// Locale.forLanguageTag equivale a Locale("es", "PE"), cuyo constructor está deprecado.
actual fun formatearSoles(valor: Double): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-PE")).format(valor)
