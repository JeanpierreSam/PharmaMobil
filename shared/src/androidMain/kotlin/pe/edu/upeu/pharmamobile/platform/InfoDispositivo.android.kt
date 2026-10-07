package pe.edu.upeu.pharmamobile.platform

import android.os.Build

actual class InfoDispositivo actual constructor() {
    actual val sistema: String = "Android"

    // RELEASE es la versión visible ("14"); SDK_INT, el nivel de API que usan las apps.
    actual val version: String = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    actual val modelo: String = "${Build.MANUFACTURER} ${Build.MODEL}"
}
