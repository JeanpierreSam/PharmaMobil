package pe.edu.upeu.pharmamobile.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

@Suppress("DEPRECATION") // security-crypto está deprecado, pero es lo que pide la guía
class AlmacenSeguroAndroid(contexto: Context) : AlmacenSeguro {

    private val llave = MasterKey.Builder(contexto)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        contexto, ARCHIVO, llave,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // commit() y no apply(): si el usuario mata la app justo después del login
    // o del cierre de sesión, el archivo ya quedó escrito.
    override fun guardarToken(token: String) {
        prefs.edit().putString(CLAVE, token).commit()
    }

    override fun leerToken(): String? = prefs.getString(CLAVE, null)

    override fun borrarToken() {
        prefs.edit().remove(CLAVE).commit()
    }

    private companion object {
        const val ARCHIVO = "sesion_segura"   // → shared_prefs/sesion_segura.xml
        const val CLAVE = "jwt"
    }
}
