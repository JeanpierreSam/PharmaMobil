package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.error.ErrorApi

fun mensajeDe(error: ErrorApi): String = when (error) {
    is ErrorApi.Validacion -> "Revisa los campos marcados."
    ErrorApi.NoEncontrado -> "No se encontró el recurso solicitado."
    is ErrorApi.Conflicto -> error.mensaje
    ErrorApi.Servidor -> "El servidor no está disponible. Inténtalo más tarde."
    ErrorApi.SinConexion -> "Sin conexión con el servidor. Revisa tu red."
    ErrorApi.TiempoAgotado -> "El servidor tardó demasiado en responder."
}
