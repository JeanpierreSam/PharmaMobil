package pe.edu.upeu.pharmamobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.data.ProductoRepositorySimulado
import pe.edu.upeu.pharmamobile.domain.query.activos
import pe.edu.upeu.pharmamobile.domain.query.bajoStock
import pe.edu.upeu.pharmamobile.presentation.clientes.ClientesScreen
import pe.edu.upeu.pharmamobile.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobile.presentation.navigation.Destino
import pe.edu.upeu.pharmamobile.presentation.navigation.PharmaNavigationAdaptativa
import pe.edu.upeu.pharmamobile.presentation.pedidos.PedidosScreen
import pe.edu.upeu.pharmamobile.presentation.producto.MensajesProducto
import pe.edu.upeu.pharmamobile.presentation.producto.ProductosMainScreen
import pe.edu.upeu.pharmamobile.presentation.theme.PharmaMobilTheme

@Composable
fun App() {
    var rutaDestinoActual by rememberSaveable { mutableStateOf(Destino.Inicio.ruta) }
    val destinoActual = Destino.valores.first { it.ruta == rutaDestinoActual }
    var modoOscuro by rememberSaveable { mutableStateOf(false) }

    val repositorio = remember { ProductoRepositorySimulado() }
    val productos by repositorio.productosFlow.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    PharmaMobilTheme(modoOscuro = modoOscuro) {
        Surface(modifier = Modifier.fillMaxSize()) {
            PharmaNavigationAdaptativa(
                destinoActual = destinoActual,
                onSeleccionarDestino = { nuevoDestino ->
                    rutaDestinoActual = nuevoDestino.ruta
                },
                modoOscuro = modoOscuro,
                onToggleModoOscuro = {
                    modoOscuro = !modoOscuro
                },
                snackbarHostState = snackbarHostState
            ) { paddingValues ->
                when (destinoActual) {
                    Destino.Inicio -> {
                        InicioScreen(
                            totalProductos = productos.size,
                            productosActivos = productos.activos().size,
                            productosBajoStock = productos.bajoStock().size,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }

                    Destino.Productos -> {
                        ProductosMainScreen(
                            productos = productos,
                            onProductoRegistrado = { productoNuevo ->
                                repositorio.agregarProducto(productoNuevo)
                                scope.launch {
                                    snackbarHostState.showSnackbar(MensajesProducto.REGISTRO_EXITOSO)
                                }
                            },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }

                    Destino.Clientes -> {
                        ClientesScreen(
                            modifier = Modifier.padding(paddingValues)
                        )
                    }

                    Destino.Pedidos -> {
                        PedidosScreen(
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}
