package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.EnvioFallido
import pe.edu.upeu.pharmamobile.domain.model.MAX_INTENTOS_ENVIO

/**
 * La fila fallida no se pierde ni se esconde: el usuario ve qué pasó y decide
 * (reintentar, corregirla editándola desde la lista o descartarla).
 */
@Composable
fun DialogoEnviosFallidos(
    fallidas: List<EnvioFallido>,
    onReintentar: () -> Unit,
    onDescartar: (Long) -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { Icon(Icons.Default.ErrorOutline, contentDescription = null) },
        title = { Text(textoFallidas(fallidas.size) ?: "Envíos") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "PharmaSoft los rechazó o fallaron $MAX_INTENTOS_ENVIO veces seguidas. " +
                        "Puedes editarlos desde la lista para corregirlos, reintentar o descartarlos.",
                    style = MaterialTheme.typography.bodySmall
                )
                fallidas.forEach { fallida ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(fallida.nombre, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = fallida.motivo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        TextButton(onClick = { onDescartar(fallida.id) }) { Text("Descartar") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onReintentar) { Text("Reintentar ahora") } },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}
