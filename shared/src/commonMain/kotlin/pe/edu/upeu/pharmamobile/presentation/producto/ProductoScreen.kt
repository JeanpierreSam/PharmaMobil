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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.presentation.components.CampoFormulario
import pe.edu.upeu.pharmamobile.presentation.components.PharmaHeader

/**
 * Formulario de alta y edición de productos.
 *
 * Sesión 08: ya no guarda estado propio. Todo vive en [FormularioProducto]
 * dentro del UiState, de modo que los errores que devuelve PharmaSoft (400,
 * validationErrors) se pintan debajo del campo correspondiente, igual que
 * los de la validación local.
 */
@Composable
fun ProductoScreen(
    formulario: FormularioProducto,
    guardando: Boolean,
    onNombreChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onActivoChange: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier,
    errorOperacion: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PharmaHeader(
            titulo = "PHARMAMOBIL",
            subtitulo = if (formulario.esEdicion) "Editar Producto" else "Registro de Producto"
        )

        CampoFormulario(
            valor = formulario.nombre,
            onValorChange = onNombreChange,
            etiqueta = "Nombre del producto",
            esError = formulario.nombreError != null,
            mensajeError = formulario.nombreError
        )

        CampoFormulario(
            valor = formulario.precio,
            onValorChange = onPrecioChange,
            etiqueta = "Precio",
            esError = formulario.precioError != null,
            mensajeError = formulario.precioError,
            tipoTeclado = KeyboardType.Decimal
        )

        CampoFormulario(
            valor = formulario.stock,
            onValorChange = onStockChange,
            etiqueta = "Stock",
            esError = formulario.stockError != null,
            mensajeError = formulario.stockError,
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
                checked = formulario.activo,
                onCheckedChange = onActivoChange
            )
        }

        // Errores que no son de un campo (409 nombre duplicado, 404, sin conexión):
        // dentro del diálogo, porque un Snackbar quedaría tapado por él.
        if (errorOperacion != null) {
            Text(
                text = errorOperacion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = onGuardar,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    guardando -> "GUARDANDO..."
                    formulario.esEdicion -> "GUARDAR CAMBIOS"
                    else -> "REGISTRAR"
                }
            )
        }
    }
}
