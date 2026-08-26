package pe.edu.upeu.pharmamobile.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * Campo de texto reutilizable del formulario.
 *
 * Composable stateless: el valor y el evento de cambio llegan desde
 * arriba (state hoisting), de modo que el componente solo dibuja y
 * notifica, sin decidir como se modifica el estado.
 *
 * @param esError marca el campo en rojo. La pantalla decide cuando
 *   activarlo para no mostrar errores antes del primer intento.
 * @param mensajeError texto de apoyo que se muestra solo si [esError].
 */
@Composable
fun CampoFormulario(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    esError: Boolean = false,
    mensajeError: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = esError,
        supportingText = {
            if (esError && mensajeError != null) {
                Text(mensajeError)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado
        ),
        modifier = modifier.fillMaxWidth()
    )
}
