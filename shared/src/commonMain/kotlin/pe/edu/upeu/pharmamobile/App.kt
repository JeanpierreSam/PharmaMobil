package pe.edu.upeu.pharmamobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobile.domain.model.EstadoSesion
import pe.edu.upeu.pharmamobile.domain.model.Permisos
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.query.activos
import pe.edu.upeu.pharmamobile.domain.query.bajoStock
import pe.edu.upeu.pharmamobile.presentation.acercade.AcercaDeScreen
import pe.edu.upeu.pharmamobile.presentation.clientes.ClientesScreen
import pe.edu.upeu.pharmamobile.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobile.presentation.navigation.Destino
import pe.edu.upeu.pharmamobile.presentation.navigation.PharmaNavigationAdaptativa
import pe.edu.upeu.pharmamobile.presentation.pedidos.PedidosScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobile.presentation.producto.ProductosMainScreen
import pe.edu.upeu.pharmamobile.presentation.sesion.LoginScreen
import pe.edu.upeu.pharmamobile.presentation.sesion.SesionViewModel
import pe.edu.upeu.pharmamobile.presentation.theme.PharmaMobilTheme

// Koin se inicia con startKoin(...) en MainApplication (Android) / initKoinIos()
// (iOS); desde Koin 4.2 ya no hace falta envolver la UI en KoinContext.
@Composable
fun App() {
    var modoOscuro by rememberSaveable { mutableStateOf(false) }
    val sesionViewModel = koinViewModel<SesionViewModel>()
    val estado by sesionViewModel.estado.collectAsStateWithLifecycle()
    val loginUi by sesionViewModel.uiState.collectAsStateWithLifecycle()

    PharmaMobilTheme(modoOscuro = modoOscuro) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (val e = estado) {
                EstadoSesion.Verificando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                is EstadoSesion.Inactiva -> LoginScreen(
                    uiState = loginUi,
                    onUsuarioChange = sesionViewModel::onUsuarioChange,
                    onClaveChange = sesionViewModel::onClaveChange,
                    onIngresar = sesionViewModel::ingresar
                )

                // key: otra sesión = otro árbol (se reinicia la ruta del menú, rememberSaveable incluido)
                is EstadoSesion.Activa -> key(e.sesion.token) {
                    AlcanceDeSesion {
                        AppAutenticada(
                            sesion = e.sesion,
                            esDepuracion = sesionViewModel.esDepuracion,
                            modoOscuro = modoOscuro,
                            onToggleModoOscuro = { modoOscuro = !modoOscuro },
                            onCerrarSesion = sesionViewModel::salir
                        )
                    }
                }
            }
        }
    }
}

/** ViewModels que viven solo mientras dura la sesión; se destruyen al salir. */
@Composable
private fun AlcanceDeSesion(contenido: @Composable () -> Unit) {
    val dueno = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(dueno) { onDispose { dueno.viewModelStore.clear() } }
    CompositionLocalProvider(LocalViewModelStoreOwner provides dueno) { contenido() }
}

@Composable
private fun AppAutenticada(
    sesion: Sesion,
    esDepuracion: Boolean,
    modoOscuro: Boolean,
    onToggleModoOscuro: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val destinos = remember(sesion) { Destino.disponiblesPara(sesion, esDepuracion) }
    var ruta by rememberSaveable { mutableStateOf(Destino.Inicio.ruta) }
    val destinoActual = destinos.firstOrNull { it.ruta == ruta } ?: Destino.Inicio
    val snackbarHostState = remember { SnackbarHostState() }

    PharmaNavigationAdaptativa(
        destinoActual = destinoActual,
        destinos = destinos,
        onSeleccionarDestino = { ruta = it.ruta },
        modoOscuro = modoOscuro,
        onToggleModoOscuro = onToggleModoOscuro,
        onCerrarSesion = onCerrarSesion,
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
            Destino.Productos -> ProductosMainScreen(
                snackbarHostState = snackbarHostState,
                puedeEscribir = sesion.puede(Permisos.CATALOGO_ESCRIBIR),
                modifier = Modifier.padding(paddingValues)
            )
            Destino.Clientes -> ClientesScreen(modifier = Modifier.padding(paddingValues))
            Destino.Pedidos -> PedidosScreen(modifier = Modifier.padding(paddingValues))
            Destino.AcercaDe -> AcercaDeScreen(modifier = Modifier.padding(paddingValues))
        }
    }
}
