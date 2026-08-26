package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.Producto

/**
 * Pantalla de registro de productos de PharmaMobil.
 *
 * Sesion 03 - Guia Practica: formulario interactivo construido con
 * Compose Multiplatform. Captura los datos, los valida y solo entonces
 * instancia un objeto del dominio [Producto].
 */
@Composable
fun ProductoScreen(modifier: Modifier = Modifier) {
    // Estado observable de cada campo. Se guardan como String porque es
    // lo que entrega el usuario; la conversion a numero ocurre al validar.
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    // Retroalimentacion del sistema y ultimo producto registrado.
    var mensaje by remember { mutableStateOf("") }
    var ultimoProducto by remember { mutableStateOf<Producto?>(null) }

    // Identificador incremental para los productos que se van creando.
    var siguienteId by remember { mutableStateOf(1L) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "PHARMAMOBIL",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Registro de Producto",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del producto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = stock,
            onValueChange = { stock = it },
            label = { Text("Stock") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                // Conversion segura: si el texto no es numerico se obtiene null
                // en lugar de lanzar una excepcion.
                val precioIngresado = precio.toDoubleOrNull()
                val stockIngresado = stock.toIntOrNull()

                mensaje = when {
                    nombre.isBlank() -> "Ingrese nombre del producto"
                    precioIngresado == null || precioIngresado <= 0.0 -> "Ingrese precio valido"
                    stockIngresado == null -> "Ingrese stock valido"
                    stockIngresado < 0 -> "El stock no puede ser negativo"
                    else -> {
                        // Solo aqui se construye el objeto del dominio: Producto
                        // valida en su init y lanzaria excepcion con datos invalidos.
                        ultimoProducto = Producto(
                            id = siguienteId,
                            nombre = nombre.trim(),
                            precio = precioIngresado,
                            stock = stockIngresado
                        )
                        siguienteId++
                        "Producto registrado correctamente"
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
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        ultimoProducto?.let { producto ->
            Text(
                text = "ID ${producto.id} | ${producto.nombre} | " +
                    "S/ ${producto.precio} | Stock: ${producto.stock}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
