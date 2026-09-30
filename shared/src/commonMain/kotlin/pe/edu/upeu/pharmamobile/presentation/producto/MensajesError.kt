package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.error.ErrorApi

fun mensajeDe(error: ErrorApi): String = when (error) {
    ErrorApi.NoEncontrado -> "No se encontró el recurso solicitado."
    ErrorApi.Servidor -> "El servidor no está disponible. Inténtalo más tarde."
    ErrorApi.SinConexion -> "Sin conexión a internet. Revisa tu red."
    ErrorApi.TiempoAgotado -> "El servidor tardó demasiado en responder."
}
