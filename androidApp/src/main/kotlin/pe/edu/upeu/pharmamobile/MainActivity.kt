package pe.edu.upeu.pharmamobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.android.ext.android.inject
import pe.edu.upeu.pharmamobile.permiso.GestorPermisosAndroid
import pe.edu.upeu.pharmamobile.permiso.PuentePermisos

class MainActivity : ComponentActivity() {

    private val gestorPermisos: GestorPermisosAndroid by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // registerForActivityResult debe registrarse antes de que la Activity arranque.
        gestorPermisos.puente = PuentePermisos(this)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        // Al rotar la pantalla esta Activity se destruye: no debe quedar referenciada.
        if (gestorPermisos.puente?.actividad === this) gestorPermisos.puente = null
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
