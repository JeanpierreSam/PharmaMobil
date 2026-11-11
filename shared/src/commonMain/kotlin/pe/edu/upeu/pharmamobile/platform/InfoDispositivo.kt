package pe.edu.upeu.pharmamobile.platform

/**
 * Datos del dispositivo para la pantalla «Acerca de». Es una expect class sin
 * dependencias: cada plataforma la completa con su propia API (android.os.Build
 * en Android, UIDevice en iOS) y el compilador exige ambos actual.
 */
expect class InfoDispositivo() {
    val sistema: String
    val version: String
    val modelo: String
}
