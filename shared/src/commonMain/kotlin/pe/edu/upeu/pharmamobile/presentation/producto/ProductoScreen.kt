package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobile.presentation.components.CampoFormulario
import pe.edu.upeu.pharmamobile.presentation.components.PharmaHeader

/**
 * Pantalla de registro de productos de PharmaMobil.
 *
 * Sesion 05: el formulario ya no valida ni construye el dominio por su
 * cuenta -- delega en [ProductoViewModel.registrar], que a su vez usa
 * `RegistrarProductoUseCase`. Aqui solo queda estado transitorio de UI
 * (el texto que el usuario va escribiendo) y el resaltado en tiempo real.
 */
@Composable
fun ProductoScreen(
    modifier: Modifier = Modifier,
    viewModel: ProductoViewModel = koinViewModel(),
    onProductoRegistrado: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Estado observable de cada campo. Se guardan como String porque es
    // lo que entrega el usuario; la conversion a numero ocurre al validar.
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf(true) }

    // Evita mostrar errores antes de que el usuario pulse REGISTRAR.
    var intentoRegistrar by remember { mutableStateOf(false) }

    // El resaltado en rojo solo se activa tras el primer intento.
    val errorNombre = intentoRegistrar && nombreEsInvalido(nombre)
    val errorPrecio = intentoRegistrar && precioEsInvalido(precio)
    val errorStock = intentoRegistrar && stockEsInvalido(stock)

    LaunchedEffect(uiState.mensaje) {
        if (uiState.mensaje == MensajesProducto.REGISTRO_EXITOSO) {
            // Limpieza del formulario tras un registro exitoso.
            nombre = ""
            precio = ""
            stock = ""
            activo = true
            intentoRegistrar = false
            onProductoRegistrado?.invoke()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PharmaHeader(
            titulo = "PHARMAMOBIL",
            subtitulo = "Registro de Producto"
        )

        CampoFormulario(
            valor = nombre,
            onValorChange = { nombre = it },
            etiqueta = "Nombre del producto",
            esError = errorNombre,
            mensajeError = MensajesProducto.NOMBRE_OBLIGATORIO
        )

        CampoFormulario(
            valor = precio,
            onValorChange = { precio = it },
            etiqueta = "Precio",
            esError = errorPrecio,
            mensajeError = if (precio.toDoubleOrNull() == null) {
                MensajesProducto.PRECIO_NO_NUMERICO
            } else {
                MensajesProducto.PRECIO_NO_POSITIVO
            },
            tipoTeclado = KeyboardType.Decimal
        )

        CampoFormulario(
            valor = stock,
            onValorChange = { stock = it },
            etiqueta = "Stock",
            esError = errorStock,
            mensajeError = if (stock.toIntOrNull() == null) {
                MensajesProducto.STOCK_NO_ENTERO
            } else {
                MensajesProducto.STOCK_NEGATIVO
            },
            tipoTeclado = KeyboardType.Number
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Producto activo",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = activo,
                onCheckedChange = { activo = it }
            )
        }

        Button(
            onClick = {
                intentoRegistrar = true
                viewModel.registrar(nombre, precio, stock, activo)
            },
            enabled = !uiState.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.guardando) "GUARDANDO..." else "REGISTRAR")
        }

        // Text reactivo: se recompone cada vez que cambia el mensaje del ViewModel.
        val mensaje = uiState.mensaje
        if (!mensaje.isNullOrBlank()) {
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyLarge,
                color = if (mensaje == MensajesProducto.REGISTRO_EXITOSO) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
