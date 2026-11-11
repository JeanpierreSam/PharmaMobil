# Matriz de pruebas en dispositivos reales

Build: `Equipo12_ProyectoFinal_Android.apk` · 1.0.0 (1) · firmado (SHA-256 `F8:C3:84:04:83:D9:29:C2…`).
Servicio: PharmaSoft en el PC del equipo, zona con cobertura de Windows (`http://192.168.137.1:8080/api/v1/`).
Fecha de las pruebas: 11-11-2026, entre las 15:25 y las 15:50.

> Se registran solo los pasos que tienen evidencia. En los demás figura «No se sacó evidencia».

## Dispositivos

| N.º | Dispositivo | Plataforma | Build instalado | Pasos ejecutados | Resultado | Responsable y fecha |
|---|---|---|---|---|---|---|
| 1 | Samsung Galaxy A56 5G (SM-A566E), Android *<versión pendiente>* | Android | `…_Android.apk` 1.0.0 | 1, 2 (sin red: sin evidencia), 3 (solo alta), 5, 6, 7 (solo el aviso de permiso), 8. **Sin evidencia:** 4 y el resto del 3 | Correcto en lo ejecutado; sin defectos observados | Jeanpierre, 11-11-2026 |
| 2 | Samsung Galaxy S24 Ultra (SM-S928B), Android *<versión pendiente>* | Android | `…_Android.apk` 1.0.0 | 5, 6, 7 (solo el aviso de permiso), 8. **Sin evidencia:** 1, 2, 3 y 4 | Correcto en lo ejecutado; sin defectos observados | Fernando (dueño del equipo), 11-11-2026 |
| 3 | (iOS — no ejecutado; ver `docs/ios-14.md`) | iOS | — | — | No ejecutado — limitación declarada | — |

## Recorrido de 8 pasos

| N.º | Paso | Cómo provocarlo en PharmaMobil | Qué se observa | A56 | S24 |
|---|---|---|---|---|---|
| 1 | Instalación desde el archivo firmado | Instalar el `.apk` sin Android Studio | Instala; nombre «PharmaMobil» | Correcto | No se sacó evidencia |
| 2 | Login correcto e incorrecto | `admin` con clave mala → luego buena; después apagar la zona con cobertura y probar | «Usuario o contraseña incorrectos.» vs «Sin conexión con el servidor…» | Correcto (clave mala y buena); sin red: no se sacó evidencia | No se sacó evidencia |
| 3 | CRUD completo con un rechazo | Alta, edición, baja; alta con nombre repetido | El 409 aparece bajo el campo **Nombre** | Solo el alta («Gingisona B Tabletas» registrado) | No se sacó evidencia |
| 4 | Operación sin privilegio | Entrar como `vendedor` | Acciones de escritura ocultas; la sesión no se cierra | No se sacó evidencia | No se sacó evidencia |
| 5 | Modo avión | Modo avión → abrir Productos → crear uno | «Pendiente de envío» y «1 cambio por enviar» | Correcto («prueba») | Correcto («Prueba amoniaco») |
| 6 | Recuperar la red | Quitar el modo avión, **sin** pulsar ⟳ | Sube solo (H7), sin duplicar | Se sincronizó pulsando ⟳ (no se probó la sincronización automática sin tocarlo) | Aviso «Sin conexión: mostrando datos guardados» y luego sincronizado, pulsando ⟳ |
| 7 | Permiso: conceder, denegar, para siempre | Pestaña *Bajo Stock* → *Activar avisos* | Los tres estados; el tercero ofrece *Abrir Ajustes* | Solo se vio el aviso del sistema; sin resultado | Solo se vio el aviso del sistema; sin resultado |
| 8 | Cerrar sesión y reabrir | Con un pendiente → *Cerrar sesión* → *Salir y descartar* → reabrir | Pide login; la lista ya no tiene datos del usuario anterior (H8) | Correcto: el pendiente «prueba 2» no volvió | Correcto: «prueba salir pendiente» no volvió |

## Evidencia

Capturas guardadas fuera del repositorio, en `Unidad 3/S14/Capturas pruebas A56 Jeanpierre` y `Capturas pruebas S24 Fernando`. Las capturas de «Acerca del teléfono» muestran IMEI, serie y número de teléfono: no se incluyen en el PDF sin recortarlas.

## Defectos

| Id | Dispositivo y versión | Pasos para reproducir | Esperado | Obtenido | Severidad | Estado |
|---|---|---|---|---|---|---|
| — | *Ninguno observado en los pasos ejecutados.* | | | | | |
