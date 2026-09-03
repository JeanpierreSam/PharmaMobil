package pe.edu.upeu.pharmamobile.presentation.theme

import kotlin.math.round

// KMP commonMain no tiene String.format; se redondea manualmente a 2 decimales.
fun Double.aSoles(): String {
    val centavos = round(this * 100).toLong()
    val soles = centavos / 100
    val resto = (centavos % 100).let { if (it < 0) -it else it }
    val restoTexto = if (resto < 10) "0$resto" else "$resto"
    return "S/ $soles.$restoTexto"
}
