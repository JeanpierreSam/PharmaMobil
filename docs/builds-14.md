# Builds firmados · Sesión 14

Rama `feature/builds-14` desde `develop` (`eb666d7`). 11-11-2026.

## Cierre de la ficha de integración de la sesión 13

| N.º | Hueco | Decisión final | Motivo / qué haría falta |
|---|---|---|---|
| H1–H5, H11 | Cola con límite, fallidas visibles, permisos, telemetría, retorno de Ajustes | **Cerrado** (S13) | — |
| H6 | Token vencido a mitad de una edición: se pierde lo escrito (el alta queda pendiente) | **Se declara** | Exige guardar un borrador antes de ir al login y restaurarlo al volver; no se puede probar hoy en dos dispositivos. Mitigación: la sesión dura 60 min y el alta sin red no se pierde |
| H7 | Al recuperar la señal no se sincroniza sola | **Se cierra hoy** | Rompe el paso 6 del recorrido («la pantalla se actualiza sola») |
| H8 | El cierre de sesión deja catálogo y pendientes del usuario anterior | **Se cierra hoy** | Rompe el paso 8 («el equipo queda limpio para otro usuario») |
| H9 | Reintento de un alta cuya respuesta se perdió | **Se descarta** (trabajo futuro) | El nombre único de PharmaSoft responde 409; la fila queda visible para descartarla |
| H10 | Editar o dar de baja un sincronizado exige red | **Se descarta** (trabajo futuro) | Exige una cola de operaciones; fuera del alcance comprometido |
| H12 | Cloud Messaging no integrado | **Se descarta** (trabajo futuro) | Es una librería nueva y un cambio en el backend: la congelación lo prohíbe. Las notificaciones locales de stock bajo cubren el criterio de capacidades nativas |
| H13 | iOS no se ha ejecutado (sin Mac) | **Se declara** | Evidencia alternativa en `docs/ios-14.md` |
| H14 | La URL `10.0.2.2` está fija: un teléfono real no llega a PharmaSoft | **Se cierra hoy** | Sin esto no hay pruebas en dispositivos reales |
| H15 | Ícono de la plantilla KMP | **Se declara** | Ícono de plantilla, cosmético. El nombre «PharmaMobil» ya es el definitivo; el ícono se cambia con *Image Asset* en Android Studio |

## Alcance congelado (11-11-2026, 13:52)

A partir de este punto no entra funcionalidad nueva, ni cambios de arquitectura o de librerías,
ni rediseños de pantalla, ni migraciones del esquema. Solo se corrigen defectos encontrados en
las pruebas de esta sesión y se ajusta la configuración de compilación.

## Artefactos y firma

| Dato | Valor |
|---|---|
| applicationId | `pe.edu.upeu.pharmamobile` (no `pe.upeu.pharmamobil`: lo exige el registro de Firebase) |
| versionName · versionCode | 1.0.0 · 1 |
| Paquete de aplicación | `Equipo12_ProyectoFinal_Android.aab` (7,24 MB) |
| Instalable de revisión | `Equipo12_ProyectoFinal_Android.apk` (3,64 MB) |
| Titular del certificado | `CN=PharmaMobil Equipo 12, OU=EP Ingenieria de Sistemas, O=Universidad Peruana Union, L=Lima, ST=Lima, C=PE` |
| Huella SHA-256 | `F8:C3:84:04:83:D9:29:C2:D5:86:7B:E4:85:15:B4:07:A8:76:A4:7E:C1:E5:D2:22:C3:51:D8:92:B4:B3:33:9B` (idéntica en el `.apk` con `apksigner` y en el `.aab` con `keytool -printcert -jarfile`) |
| Validez | 11-11-2026 14:38 – 29-03-2054 |
| Esquemas de firma (APK) | v2 true · v3 false (v1 false, v4 false) |
| Copia del almacén | Carpeta privada de Google Drive del titular (fuera de la laptop); no se comparte ni se versiona |
| URL de PharmaSoft del build | `http://192.168.137.1:8080/api/v1/` (zona con cobertura de Windows) |
| Minificación | Activada (R8). Ver la sección siguiente |

Estado al 11-11-2026: la configuración de firma está lista (`keystore.properties`, ignorado por Git);
sin ese archivo, `assembleRelease` produce `androidApp-release-unsigned.apk`. Los artefactos firmados
se generaron el 11-11-2026 a las 14:58 con el almacén del equipo.

## Minificación (11-11-2026)

| Prueba | Resultado |
|---|---|
| Primera compilación con R8 sin reglas | **Compiló.** No hubo «Missing classes» ni `missing_rules.txt` (AGP 9.0.1, Ktor 3.6). Solo avisos de R8 por metadatos de Kotlin más nuevos que su versión |
| Reglas agregadas | Solo `-keepattributes SourceFile,LineNumberTable` y `-renamesourcefileattribute SourceFile` (trazas legibles en Crashlytics). No se agregaron `-dontwarn` ni reglas de serialización: nada las pidió |
| Recorrido completo con el APK de entrega | *Pendiente en dispositivo real*: confirmar que la lista carga (serialización) y que todas las pantallas abren (inyección). Si falla, agregar el bloque de serialización de la guía |
| Decisión | Minificación **activada** |

> Equipo 12: es el número de lista; no se asignó número de equipo.
