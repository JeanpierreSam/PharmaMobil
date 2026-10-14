package pe.edu.upeu.pharmamobile.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import pe.edu.upeu.pharmamobile.domain.model.Permisos
import pe.edu.upeu.pharmamobile.domain.model.Sesion

/**
 * Destinos principales de navegación de PharmaMobil. Cada destino declara el
 * permiso que necesita; [disponiblesPara] filtra el menú según la sesión.
 */
sealed class Destino(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector,
    val permiso: String? = null,
    val soloDepuracion: Boolean = false
) {
    data object Inicio : Destino("inicio", "Inicio", Icons.Default.Home)
    data object Productos : Destino("productos", "Productos", Icons.Default.ShoppingCart, Permisos.CATALOGO_LEER)
    data object Clientes : Destino("clientes", "Clientes", Icons.Default.Person, Permisos.CLIENTES_LEER)
    data object Pedidos : Destino("pedidos", "Pedidos", Icons.AutoMirrored.Filled.List, Permisos.VENTAS_LEER)
    data object AcercaDe : Destino("acerca-de", "Acerca de", Icons.Default.Info)

    companion object {
        val valores get() = listOf(Inicio, Productos, Clientes, Pedidos, AcercaDe)

        fun disponiblesPara(sesion: Sesion, esDepuracion: Boolean): List<Destino> =
            valores.filter { d ->
                (d.permiso == null || sesion.puede(d.permiso)) && (!d.soloDepuracion || esDepuracion)
            }
    }
}
