package pe.edu.upeu.pharmamobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobile.domain.query.activos
import pe.edu.upeu.pharmamobile.domain.query.bajoStock
import pe.edu.upeu.pharmamobile.presentation.clientes.ClientesScreen
import pe.edu.upeu.pharmamobile.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobile.presentation.navigation.Destino
import pe.edu.upeu.pharmamobile.presentation.navigation.PharmaNavigationAdaptativa
import pe.edu.upeu.pharmamobile.presentation.pedidos.PedidosScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobile.presentation.producto.ProductosMainScreen
import pe.edu.upeu.pharmamobile.presentation.theme.PharmaMobilTheme

// Koin se inicia con startKoin(...) en MainApplication (Android) / initKoinIos()
// (iOS); desde Koin 4.2 ya no hace falta envolver la UI en KoinContext.
@Composable
fun App() {
    var rutaDestinoActual by rememberSaveable { mutableStateOf(Destino.Inicio.ruta) }
    val destinoActual = Destino.valores.first { it.ruta == rutaDestinoActual }
    var modoOscuro by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

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
                        // Mismo ProductoViewModel (misma ViewModelStoreOwner) que
                        // resuelve ProductosMainScreen: el resumen queda sincronizado.
                        val productoViewModel = koinViewModel<ProductoViewModel>()
                        val uiState by productoViewModel.uiState.collectAsStateWithLifecycle()

                        InicioScreen(
                            totalProductos = uiState.productos.size,
                            productosActivos = uiState.productos.activos().size,
                            productosBajoStock = uiState.productos.bajoStock().size,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }

                    Destino.Productos -> {
                        ProductosMainScreen(modifier = Modifier.padding(paddingValues))
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
