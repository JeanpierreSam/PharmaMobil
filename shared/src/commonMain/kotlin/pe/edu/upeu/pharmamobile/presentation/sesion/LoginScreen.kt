package pe.edu.upeu.pharmamobile.presentation.sesion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/** No conoce el token: solo dibuja el formulario y avisa al ViewModel. */
@Composable
fun LoginScreen(
    uiState: SesionUiState,
    onUsuarioChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onIngresar: () -> Unit,
    modifier: Modifier = Modifier,
    extraDepuracion: (@Composable () -> Unit)? = null   // Parte B
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PharmaMobil", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text("Inicia sesión con tu usuario de PharmaSoft", style = MaterialTheme.typography.bodyMedium)

        uiState.aviso?.let { aviso ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(aviso, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        OutlinedTextField(
            value = uiState.usuario,
            onValueChange = onUsuarioChange,
            label = { Text("Usuario") },
            singleLine = true,
            enabled = !uiState.cargando,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.clave,
            onValueChange = onClaveChange,
            label = { Text("Contraseña") },
            singleLine = true,
            enabled = !uiState.cargando,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onIngresar() }),
            modifier = Modifier.fillMaxWidth()
        )

        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = onIngresar,
            enabled = !uiState.cargando && uiState.puedeIngresar,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.cargando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Ingresar")
            }
        }

        extraDepuracion?.invoke()
    }
}
