package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.platform.formatearSoles

/** El texto se arma una sola vez en código común; solo el envío depende de la plataforma. */
fun Producto.comoTextoParaCompartir(): String =
    "$nombre — ${formatearSoles(precio)} — Stock: $stock"
