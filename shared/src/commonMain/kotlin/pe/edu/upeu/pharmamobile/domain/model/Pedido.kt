package pe.edu.upeu.pharmamobile.domain.model

data class Pedido(
    val id:Long,
    val cliente: String,
    val detalles: List<DetallePedido>,
    val estado: EstadoPedido
){
    // Cabecera transaccional: total global del pedido
    fun total(): Double = detalles.sumOf { it.subtotal() }

    fun cantidadItems(): Int = detalles.sumOf { it.cantidad }

    // when exhaustivo sobre la sealed class: el compilador obliga a cubrir todas las ramas
    fun descripcionEstado(): String = when (estado) {
        EstadoPedido.Pendiente -> "Pedido pendiente de confirmación"
        EstadoPedido.Procesando -> "Pedido en preparación"
        EstadoPedido.Entregando -> "Pedido en camino"
        EstadoPedido.Atrasado -> "Pedido atrasado"
        is EstadoPedido.Rechazado -> "Rechazado: ${estado.motivo}"
    }
}
