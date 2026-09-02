package pe.edu.upeu.pharmamobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.presentation.navigation.Destino
import pe.edu.upeu.pharmamobile.presentation.navigation.PharmaAppScaffold
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoScreen

@Composable
fun App() {
    var destinoActual by remember { mutableStateOf<Destino>(Destino.Inicio) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PharmaAppScaffold(
                destinoActual = destinoActual,
                onSeleccionarDestino = { nuevoDestino ->
                    destinoActual = nuevoDestino
                }
            ) { paddingValues ->
                when (destinoActual) {
                    Destino.Inicio -> {
                        PantallaMarcador(
                            titulo = "Bienvenido a PharmaMobil",
                            subtitulo = "Resumen operativo del sistema farmacéutico",
                            modifier = Modifier.padding(paddingValues)
                        )
                    }

                    Destino.Productos -> {
                        // Reutilización directa de ProductoScreen sin duplicar la lógica
                        ProductoScreen(
                            modifier = Modifier
                                .padding(paddingValues)
                                .verticalScroll(rememberScrollState())
                        )
                    }

                    Destino.Clientes -> {
                        PantallaMarcador(
                            titulo = "Gestión de Clientes",
                            subtitulo = "Módulo demostrativo para control de clientes y contactos",
                            modifier = Modifier.padding(paddingValues)
                        )
                    }

                    Destino.Pedidos -> {
                        PantallaMarcador(
                            titulo = "Control de Pedidos",
                            subtitulo = "Módulo demostrativo para órdenes de compra y despachos",
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PantallaMarcador(
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
