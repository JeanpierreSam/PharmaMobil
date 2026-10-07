package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.query.activos
import pe.edu.upeu.pharmamobile.domain.query.bajoStock
import pe.edu.upeu.pharmamobile.domain.query.inactivos
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUiState.Fase
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUiState.Operacion

/**
 * Pestañas de clasificación de inventario de productos.
 */
enum class TabProducto(val titulo: String) {
    ACTIVOS("Activos"),
    INACTIVOS("Inactivos"),
    BAJO_STOCK("Bajo Stock")
}

/**
 * Pantalla principal de Productos: listado por pestañas y CRUD contra PharmaSoft.
 *
 * Sesión 08: el `when` sobre [Fase] es exhaustivo (sin else) y las operaciones
 * se reflejan en [Operacion], así que guardar o eliminar no oculta la lista.
 * Los resultados (éxito o fallo) se muestran en el Snackbar de la app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosMainScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: ProductoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var tabSeleccionada by rememberSaveable { mutableIntStateOf(0) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    // Un solo canal de avisos: éxito de la operación o su fallo (409, sin conexión, etc.).
    // Con el formulario abierto el fallo se muestra dentro del diálogo, no en el Snackbar.
    val falloOperacion = (uiState.operacion as? Operacion.Fallida)?.mensaje
    val aviso = uiState.mensajeExito ?: falloOperacion.takeIf { !uiState.formulario.abierto }
    LaunchedEffect(aviso) {
        if (aviso != null) {
            snackbarHostState.showSnackbar(aviso)
            viewModel.mensajeMostrado()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val fase = uiState.fase) {
            Fase.Cargando -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Fase.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No se pudo cargar el inventario: ${fase.mensaje}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = viewModel::cargarProductos) {
                        Text("Reintentar")
                    }
                }
            }

            Fase.SinProductos -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Todavía no hay productos registrados.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            is Fase.ConProductos -> {
                val productosFiltrados = remember(fase.productos, tabSeleccionada) {
                    when (TabProducto.entries[tabSeleccionada]) {
                        TabProducto.ACTIVOS -> fase.productos.activos()
                        TabProducto.INACTIVOS -> fase.productos.inactivos()
                        TabProducto.BAJO_STOCK -> fase.productos.bajoStock()
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    PrimaryTabRow(selectedTabIndex = tabSeleccionada) {
                        TabProducto.entries.forEachIndexed { index, tab ->
                            Tab(
                                selected = tabSeleccionada == index,
                                onClick = { tabSeleccionada = index },
                                text = { Text(tab.titulo) }
                            )
                        }
                    }

                    if (productosFiltrados.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay productos en esta categoría.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = productosFiltrados,
                                key = { it.id }
                            ) { producto ->
                                TarjetaProducto(
                                    producto = producto.toUi(),
                                    accionesHabilitadas = uiState.operacion !is Operacion.EnCurso,
                                    onEditar = { viewModel.editar(producto.id) },
                                    onEliminar = { productoAEliminar = producto }
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = viewModel::nuevoProducto,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Registrar nuevo producto"
            )
        }
    }

    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Dar de baja") },
            text = { Text("¿Dar de baja \"${producto.nombre}\"? Pasará a la pestaña Inactivos.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(producto.id)
                    productoAEliminar = null
                }) { Text("Dar de baja") }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    if (uiState.formulario.abierto) {
        Dialog(onDismissRequest = viewModel::cerrarFormulario) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.formulario.esEdicion) "Editar Producto" else "Nuevo Producto",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = viewModel::cerrarFormulario) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ProductoScreen(
                        formulario = uiState.formulario,
                        guardando = uiState.guardando,
                        onNombreChange = viewModel::onNombreChange,
                        onPrecioChange = viewModel::onPrecioChange,
                        onStockChange = viewModel::onStockChange,
                        onActivoChange = viewModel::onActivoChange,
                        onGuardar = viewModel::guardar,
                        modifier = Modifier.fillMaxWidth(),
                        errorOperacion = falloOperacion
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaProducto(
    producto: ProductoUi,
    accionesHabilitadas: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val esBajoStock = producto.requiereReposicion

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esBajoStock) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = if (producto.activo) "Activo" else "Inactivo",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (producto.activo) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Precio: ${producto.precio}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Stock: ${producto.stock}" + if (esBajoStock) " (Bajo Stock)" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (esBajoStock) FontWeight.Bold else FontWeight.Normal,
                        color = if (esBajoStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onEditar, enabled = accionesHabilitadas) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar ${producto.nombre}")
                }
                IconButton(onClick = onEliminar, enabled = accionesHabilitadas) {
                    Icon(Icons.Default.Delete, contentDescription = "Dar de baja ${producto.nombre}")
                }
            }
        }
    }
}
