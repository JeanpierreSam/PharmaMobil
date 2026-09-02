package pe.edu.upeu.pharmamobile.presentation.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Contenedor de navegación adaptativa para PharmaMobil.
 *
 * Selecciona automáticamente la estructura de navegación según el ancho disponible:
 * - Teléfono (< 600dp): ModalNavigationDrawer
 * - Tablet (600dp .. 840dp): NavigationRail
 * - Escritorio (> 840dp): PermanentNavigationDrawer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmaNavigationAdaptativa(
    destinoActual: Destino,
    onSeleccionarDestino: (Destino) -> Unit,
    modoOscuro: Boolean,
    onToggleModoOscuro: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val anchoDp = maxWidth

        when {
            // Factor de forma Escritorio / Pantalla Ancha (> 840dp)
            anchoDp >= 840.dp -> {
                PermanentNavigationDrawer(
                    drawerContent = {
                        PermanentDrawerSheet {
                            PharmaDrawerContent(
                                destinoActual = destinoActual,
                                onSeleccionarDestino = onSeleccionarDestino
                            )
                        }
                    }
                ) {
                    ScaffoldTopBarAdaptativo(
                        destinoActual = destinoActual,
                        mostrarBotonMenu = false,
                        onAbrirMenu = {},
                        modoOscuro = modoOscuro,
                        onToggleModoOscuro = onToggleModoOscuro,
                        content = content
                    )
                }
            }

            // Factor de forma Tablet (600dp a 840dp)
            anchoDp >= 600.dp -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail {
                        Destino.valores.forEach { destino ->
                            NavigationRailItem(
                                selected = destinoActual == destino,
                                onClick = { onSeleccionarDestino(destino) },
                                icon = {
                                    Icon(
                                        imageVector = destino.icono,
                                        contentDescription = destino.titulo
                                    )
                                },
                                label = { Text(destino.titulo) }
                            )
                        }
                    }

                    ScaffoldTopBarAdaptativo(
                        destinoActual = destinoActual,
                        mostrarBotonMenu = false,
                        onAbrirMenu = {},
                        modoOscuro = modoOscuro,
                        onToggleModoOscuro = onToggleModoOscuro,
                        modifier = Modifier.weight(1f),
                        content = content
                    )
                }
            }

            // Factor de forma Teléfono (< 600dp)
            else -> {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        PharmaDrawerContent(
                            destinoActual = destinoActual,
                            onSeleccionarDestino = { destino ->
                                onSeleccionarDestino(destino)
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    ScaffoldTopBarAdaptativo(
                        destinoActual = destinoActual,
                        mostrarBotonMenu = true,
                        onAbrirMenu = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        },
                        modoOscuro = modoOscuro,
                        onToggleModoOscuro = onToggleModoOscuro,
                        content = content
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScaffoldTopBarAdaptativo(
    destinoActual: Destino,
    mostrarBotonMenu: Boolean,
    onAbrirMenu: () -> Unit,
    modoOscuro: Boolean,
    onToggleModoOscuro: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(destinoActual.titulo) },
                navigationIcon = {
                    if (mostrarBotonMenu) {
                        IconButton(onClick = onAbrirMenu) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleModoOscuro) {
                        Icon(
                            imageVector = if (modoOscuro) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                            contentDescription = if (modoOscuro) "Cambiar a modo claro" else "Cambiar a modo oscuro"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}
