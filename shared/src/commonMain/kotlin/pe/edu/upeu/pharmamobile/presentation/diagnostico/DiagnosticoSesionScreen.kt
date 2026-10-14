package pe.edu.upeu.pharmamobile.presentation.diagnostico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobile.data.remote.ResultadoPrueba

/** Destino «Diagnóstico de sesión» del menú (solo depuración). */
@Composable
fun DiagnosticoSesionScreen(
    modifier: Modifier = Modifier,
    viewModel: DiagnosticoSesionViewModel = koinViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Herramienta de depuración: no existe en la versión publicada.", style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = viewModel::probarEscenarios, enabled = !estado.ejecutando, modifier = Modifier.fillMaxWidth()) {
            Text("Escenarios 1, 3, 4 y 5 (cabecera manual)")
        }
        OutlinedButton(onClick = viewModel::alterarTokenGuardado, modifier = Modifier.fillMaxWidth()) {
            Text("Escenario 3 en la app: alterar el token guardado")
        }
        OutlinedButton(onClick = viewModel::probarMatriz, enabled = !estado.ejecutando, modifier = Modifier.fillMaxWidth()) {
            Text("Matriz de permisos con el usuario actual")
        }
        Resultados(estado)
    }
}

/** Se muestra en el login (sin sesión): escenario 4 y efecto 2 del cierre de sesión. */
@Composable
fun PruebaSinSesion(viewModel: DiagnosticoSesionViewModel = koinViewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = viewModel::probarSinSesion, enabled = !estado.ejecutando) {
            Text("[Depuración] Lanzar GET /productos sin sesión")
        }
        Resultados(estado)
    }
}

@Composable
private fun Resultados(estado: DiagnosticoUiState) {
    if (estado.ejecutando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    estado.nota?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
    estado.resultados.forEach { TarjetaResultado(it) }
}

@Composable
private fun TarjetaResultado(r: ResultadoPrueba) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(r.prueba, fontWeight = FontWeight.Bold)
            Text("HTTP ${r.codigo ?: "—"}", style = MaterialTheme.typography.titleMedium)
            Text("message: ${r.mensaje}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
