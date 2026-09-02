package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.components.CampoFormulario
import pe.edu.upeu.pharmamobile.presentation.components.PharmaHeader

/**
 * Pantalla de registro de productos de PharmaMobil.
 *
 * Sesion 03: formulario interactivo construido con Compose Multiplatform.
 * Captura los datos como texto, los valida de forma estricta y solo
 * entonces instancia un objeto del dominio [Producto].
 */
@Composable
fun ProductoScreen(
    modifier: Modifier = Modifier,
    siguienteIdInicial: Long = 1L,
    onProductoCreado: ((Producto) -> Unit)? = null
) {
    // Estado observable de cada campo. Se guardan como String porque es
    // lo que entrega el usuario; la conversion a numero ocurre al validar.
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    // Retroalimentacion del sistema y ultimo producto registrado.
    var mensaje by remember { mutableStateOf("") }
    var ultimoProducto by remember { mutableStateOf<Producto?>(null) }

    // Evita mostrar errores antes de que el usuario pulse REGISTRAR.
    var intentoRegistrar by remember { mutableStateOf(false) }

    // Identificador incremental para los productos que se van creando.
    var siguienteId by remember(siguienteIdInicial) { mutableStateOf(siguienteIdInicial) }

    // El resaltado en rojo solo se activa tras el primer intento.
    val errorNombre = intentoRegistrar && nombreEsInvalido(nombre)
    val errorPrecio = intentoRegistrar && precioEsInvalido(precio)
    val errorStock = intentoRegistrar && stockEsInvalido(stock)

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

        Button(
            onClick = {
                intentoRegistrar = true

                when (val resultado = validarYCrearProducto(siguienteId, nombre, precio, stock)) {
                    is ResultadoRegistro.Invalido -> {
                        mensaje = resultado.mensaje
                    }

                    is ResultadoRegistro.Exitoso -> {
                        ultimoProducto = resultado.producto
                        siguienteId++
                        mensaje = MensajesProducto.REGISTRO_EXITOSO
                        onProductoCreado?.invoke(resultado.producto)

                        // Limpieza del formulario: Compose recompone y deja
                        // los campos listos para un nuevo registro.
                        nombre = ""
                        precio = ""
                        stock = ""
                        intentoRegistrar = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("REGISTRAR")
        }

        // Text reactivo: se recompone cada vez que cambia el estado mensaje.
        if (mensaje.isNotBlank()) {
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

        ultimoProducto?.let { producto ->
            Text(
                text = "ID ${producto.id}  |  ${producto.nombre}\n" +
                    "S/ ${producto.precio}  |  Stock: ${producto.stock}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
