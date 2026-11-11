package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Franja superior de Productos. Responde sin abrir ningún menú: si hay conexión,
 * cuántos cambios faltan por enviar y de cuándo son los datos. Si hay filas
 * fallidas, ofrece una acción («Revisar»), no solo un color.
 */
@Composable
fun BarraSincronizacion(
    antiguedad: String,
    aviso: String?,
    pendientes: Int,
    fallidas: Int,
    sincronizando: Boolean,
    onSincronizar: () -> Unit,
    onRevisarFallidas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hayProblema = aviso != null || fallidas > 0
    Surface(
        color = if (hayProblema) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (aviso != null) {
                Icon(Icons.Default.CloudOff, contentDescription = null, modifier = Modifier.padding(end = 12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (sincronizando) "Sincronizando…" else antiguedad,
                    style = MaterialTheme.typography.labelLarge
                )
                lineaEstado(aviso, pendientes)?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
                textoFallidas(fallidas)?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (fallidas > 0 && !sincronizando) {
                TextButton(onClick = onRevisarFallidas) { Text("Revisar") }
            }
            if (sincronizando) {
                CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(24.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = onSincronizar) {
                    Icon(Icons.Default.Refresh, contentDescription = "Sincronizar")
                }
            }
        }
    }
}
