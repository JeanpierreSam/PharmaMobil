package pe.edu.upeu.pharmamobile.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

/**
 * Contenedor Scaffold con TopAppBar dinámica y ModalNavigationDrawer para PharmaMobil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmaAppScaffold(
    destinoActual: Destino,
    onSeleccionarDestino: (Destino) -> Unit,
    modifier: Modifier = Modifier,
    accionesTopBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PharmaDrawerContent(
                destinoActual = destinoActual,
                onSeleccionarDestino = { destino ->
                    onSeleccionarDestino(destino)
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        },
        modifier = modifier
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(destinoActual.titulo) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú de navegación"
                            )
                        }
                    },
                    actions = {
                        accionesTopBar()
                    }
                )
            }
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}
