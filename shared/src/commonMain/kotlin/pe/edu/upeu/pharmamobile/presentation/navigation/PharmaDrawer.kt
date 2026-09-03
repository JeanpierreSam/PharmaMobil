package pe.edu.upeu.pharmamobile.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Contenido del drawer de navegación (marca + items) sin ningún "sheet" contenedor.
 *
 * Se usa desnudo dentro de [PharmaDrawerContent] (teléfono) y también dentro de un
 * PermanentDrawerSheet en escritorio, para no anidar dos superficies de drawer distintas.
 */
@Composable
fun PharmaDrawerContenidoInterno(
    destinoActual: Destino,
    onSeleccionarDestino: (Destino) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "PharmaMobil",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sistema Farmacéutico empresarial",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Destino.valores.forEach { destino ->
            val seleccionado = destinoActual == destino
            NavigationDrawerItem(
                label = { Text(destino.titulo) },
                icon = {
                    Icon(
                        imageVector = destino.icono,
                        contentDescription = destino.titulo
                    )
                },
                selected = seleccionado,
                onClick = { onSeleccionarDestino(destino) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

/**
 * Drawer de navegación lateral para PharmaMobil (variante modal, teléfono).
 */
@Composable
fun PharmaDrawerContent(
    destinoActual: Destino,
    onSeleccionarDestino: (Destino) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        PharmaDrawerContenidoInterno(
            destinoActual = destinoActual,
            onSeleccionarDestino = onSeleccionarDestino
        )
    }
}
