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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import pe.edu.upeu.pharmamobile.domain.model.Producto

/**
 * Pestañas de clasificación de inventario de productos.
 */
enum class TabProducto(val titulo: String) {
    ACTIVOS("Activos"),
    INACTIVOS("Inactivos"),
    BAJO_STOCK("Bajo Stock")
}

/**
 * Pantalla principal de Productos con Tabs y formulario de registro integrado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosMainScreen(
    productos: List<Producto>,
    onProductoRegistrado: (Producto) -> Unit,
    modifier: Modifier = Modifier
) {
    var tabSeleccionada by remember { mutableIntStateOf(0) }
    var mostrarFormularioRegistro by remember { mutableStateOf(false) }

    val productosFiltrados = remember(productos, tabSeleccionada) {
        when (TabProducto.entries[tabSeleccionada]) {
            TabProducto.ACTIVOS -> productos.filter { it.activo }
            TabProducto.INACTIVOS -> productos.filter { !it.activo }
            TabProducto.BAJO_STOCK -> productos.filter { it.stock <= 5 }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = productosFiltrados,
                        key = { it.id }
                    ) { producto ->
                        TarjetaProducto(producto = producto)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { mostrarFormularioRegistro = true },
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

    if (mostrarFormularioRegistro) {
        Dialog(onDismissRequest = { mostrarFormularioRegistro = false }) {
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
                            text = "Nuevo Producto",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { mostrarFormularioRegistro = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Formulario de registro original reutilizado
                    ProductoScreen(
                        modifier = Modifier.fillMaxWidth(),
                        onProductoCreado = { productoNuevo ->
                            onProductoRegistrado(productoNuevo)
                            mostrarFormularioRegistro = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaProducto(producto: Producto) {
    val esBajoStock = producto.stock <= 5

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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (producto.activo) "Activo" else "Inactivo",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (producto.activo) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Precio: S/ ${producto.precio}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Stock: ${producto.stock}" + if (esBajoStock) " (Bajo Stock)" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (esBajoStock) FontWeight.Bold else FontWeight.Normal,
                    color = if (esBajoStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
