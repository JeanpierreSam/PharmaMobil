package pe.edu.upeu.pharmamobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import pe.edu.upeu.pharmamobile.data.ProductoRepositorySimulado
import pe.edu.upeu.pharmamobile.presentation.clientes.ClientesScreen
import pe.edu.upeu.pharmamobile.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobile.presentation.navigation.Destino
import pe.edu.upeu.pharmamobile.presentation.navigation.PharmaNavigationAdaptativa
import pe.edu.upeu.pharmamobile.presentation.pedidos.PedidosScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductosMainScreen
import pe.edu.upeu.pharmamobile.presentation.theme.PharmaMobilTheme

@Composable
fun App() {
    var destinoActual by remember { mutableStateOf<Destino>(Destino.Inicio) }
    var modoOscuro by remember { mutableStateOf(false) }

    val repositorio = remember { ProductoRepositorySimulado() }
    val productos by repositorio.productosFlow.collectAsState()

    PharmaMobilTheme(modoOscuro = modoOscuro) {
        Surface(modifier = Modifier.fillMaxSize()) {
            PharmaNavigationAdaptativa(
                destinoActual = destinoActual,
                onSeleccionarDestino = { nuevoDestino ->
                    destinoActual = nuevoDestino
                },
                modoOscuro = modoOscuro,
                onToggleModoOscuro = {
                    modoOscuro = !modoOscuro
                }
            ) { paddingValues ->
                when (destinoActual) {
                    Destino.Inicio -> {
                        InicioScreen(
                            totalProductos = productos.size,
                            productosActivos = productos.count { it.activo },
                            productosBajoStock = productos.count { it.stock <= 5 },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }

                    Destino.Productos -> {
                        ProductosMainScreen(
                            productos = productos,
                            onProductoRegistrado = { productoNuevo ->
                                repositorio.agregarProducto(productoNuevo)
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
