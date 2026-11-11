package pe.edu.upeu.pharmamobile.presentation.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUi

/** Diálogo de detalle: se muestra mientras el ViewModel no esté en Cerrado. */
@Composable
fun DetalleProductoScreen(viewModel: DetalleProductoViewModel = koinViewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    if (estado == DetalleUiState.Cerrado) return

    Dialog(onDismissRequest = viewModel::cerrar) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalle del producto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = viewModel::cerrar) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(Modifier.height(8.dp))

                when (val e = estado) {
                    DetalleUiState.Cerrado -> Unit
                    DetalleUiState.Cargando -> Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                    is DetalleUiState.Error -> Text(
                        text = "No se pudo cargar el producto: ${e.mensaje}",
                        color = MaterialTheme.colorScheme.error
                    )
                    is DetalleUiState.Listo -> ContenidoDetalle(
                        producto = e.ui,
                        onCompartir = { viewModel.compartir(e.producto) }
                    )
                }
            }
        }
    }
}

/** El composable solo conoce la acción onCompartir, no la plataforma que la resuelve. */
@Composable
private fun ContenidoDetalle(producto: ProductoUi, onCompartir: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(producto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        HorizontalDivider()
        FilaDetalle("Código", "#${producto.id}")
        FilaDetalle("Precio", producto.precio)
        FilaDetalle("Stock", producto.stock.toString() + if (producto.requiereReposicion) " (Bajo Stock)" else "")
        FilaDetalle("Estado", if (producto.activo) "Activo" else "Inactivo")
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onCompartir() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Compartir")
        }
    }
}

@Composable
private fun FilaDetalle(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}
