package pe.edu.upeu.pharmamobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            // safeContentPadding respeta barras de sistema y notch;
            // verticalScroll evita que el teclado tape el formulario.
            ProductoScreen(
                modifier = Modifier
                    .safeContentPadding()
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}
