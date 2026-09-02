package pe.edu.upeu.pharmamobile.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Destinos principales de navegación de PharmaMobil.
 */
sealed class Destino(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
) {
    data object Inicio : Destino("inicio", "Inicio", Icons.Default.Home)
    data object Productos : Destino("productos", "Productos", Icons.Default.ShoppingCart)
    data object Clientes : Destino("clientes", "Clientes", Icons.Default.Person)
    data object Pedidos : Destino("pedidos", "Pedidos", Icons.AutoMirrored.Filled.List)

    companion object {
        val valores get() = listOf(Inicio, Productos, Clientes, Pedidos)
    }
}
