package pe.edu.upeu.pharmamobile.presentation.avisos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso

@Composable
fun TarjetaAvisosStock(
    productosBajoStock: Int,
    modifier: Modifier = Modifier,
    viewModel: AvisosStockViewModel = koinViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    // Al volver de Ajustes la pantalla se reanuda: se vuelve a consultar el estado.
    // Sin esto, el permiso activado en Ajustes no se reflejaba hasta cambiar de pestaña (H11).
    LifecycleResumeEffect(Unit) {
        viewModel.actualizarEstado()
        onPauseOrDispose { }
    }

    Card(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (estado) {
                        EstadoPermiso.Concedido -> Icons.Default.NotificationsActive
                        EstadoPermiso.DenegadoParaSiempre -> Icons.Default.NotificationsOff
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text("Avisos de stock bajo", style = MaterialTheme.typography.titleSmall)
            }
            when (estado) {
                null -> Unit
                EstadoPermiso.Concedido -> {
                    Text(
                        "Avisos activados. Te avisaremos cuando un producto tenga " +
                            "${Producto.STOCK_MINIMO} unidades o menos.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(
                        onClick = { viewModel.avisarAhora(productosBajoStock) },
                        enabled = productosBajoStock > 0
                    ) { Text("Enviar aviso ahora") }
                }
                EstadoPermiso.Denegado -> {
                    // Explicar ANTES de pedir: el usuario sabe para qué sirve el diálogo que verá.
                    Text(
                        "PharmaMobil puede avisarte cuando un producto llegue a ${Producto.STOCK_MINIMO} " +
                            "unidades o menos. Para eso necesita permiso para mostrar notificaciones.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(onClick = viewModel::activar) { Text("Activar avisos") }
                }
                EstadoPermiso.DenegadoParaSiempre -> {
                    Text(
                        "Los avisos están bloqueados. Puedes activarlos en " +
                            "Ajustes › Apps › PharmaMobil › Notificaciones. La lista sigue disponible.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(onClick = viewModel::abrirAjustes) { Text("Abrir Ajustes") }
                }
            }
        }
    }
}
