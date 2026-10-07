package pe.edu.upeu.pharmamobile.platform

import platform.UIKit.UIDevice

actual class InfoDispositivo actual constructor() {
    private val dispositivo = UIDevice.currentDevice

    actual val sistema: String = dispositivo.systemName

    actual val version: String = dispositivo.systemVersion

    // iOS no expone el nombre comercial: model devuelve la familia ("iPhone", "iPad").
    actual val modelo: String = dispositivo.model
}
