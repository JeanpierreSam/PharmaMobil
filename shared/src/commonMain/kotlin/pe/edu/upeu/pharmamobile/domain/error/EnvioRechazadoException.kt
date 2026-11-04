package pe.edu.upeu.pharmamobile.domain.error

/** Se reporta a la telemetría cuando una sincronización deja filas fallidas. */
class EnvioRechazadoException(val cantidad: Int) :
    Exception("$cantidad envío(s) rechazado(s) o agotado(s) al sincronizar")
