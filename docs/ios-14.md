# iOS · Firma y archivo (sesión 14)

## Estado al 11-11-2026

| Pieza | Estado |
|---|---|
| Código compartido para iPhone (`iosArm64`) | Compila desde Windows (`:shared:compileKotlinIosArm64`, BUILD SUCCESSFUL el 11-11-2026) |
| Proyecto Xcode | Versionado en 1.0.0 (1); `PRODUCT_BUNDLE_IDENTIFIER = pe.edu.upeu.pharmamobile.PharmaMobile$(TEAM_ID)` |
| Firma, archivo (`Product → Archive`) e `.ipa` | **No realizados**: el equipo no dispone de una Mac con Xcode |
| Prueba en iPhone real | **No realizada** por el mismo motivo |

## Qué se intentó

*Pendiente: escribir lo que de verdad se hizo (p. ej. «se buscó una Mac en el laboratorio X el día Y»
o «ningún integrante tiene Mac ni iPhone»).*

## Qué haría falta (pasos en una Mac)

1. Xcode → abrir `iosApp/iosApp.xcodeproj` → *Signing & Capabilities* → elegir el equipo (un Apple ID gratuito basta para el propio iPhone). Escribir el Team ID en `Config.xcconfig` (`TEAM_ID=`).
2. *Build Settings → Other Linker Flags*: `-lsqlite3` (driver nativo de SQLDelight).
3. Agregar `GoogleService-Info.plist` y los paquetes FirebaseCore/Crashlytics/Analytics, y llamar a `initializeFirebase()` al iniciar.
4. Destino **Any iOS Device (arm64)** → *Product → Archive* → *Organizer*.
5. Con cuenta gratuita no hay distribución ad hoc ni exportación de `.ipa` y el perfil caduca a los 7 días: se entregaría el archivo de Xcode, la captura del Organizer y un video en el iPhone.

## Limitaciones conocidas del código de iOS

- `MonitorRedIos` asume red (sin `NWPathMonitor`): la sincronización al volver la señal es manual con ⟳.
- La URL de iOS es `http://localhost:8080/api/v1/` (simulador). Un iPhone real necesitaría `http://192.168.137.1:8080/api/v1/`.
