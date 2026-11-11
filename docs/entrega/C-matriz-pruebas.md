# Matriz de pruebas en dispositivos reales

Build: `Equipo12_ProyectoFinal_Android.apk` · 1.0.0 (1) · firmado (SHA-256 `<primeros 16 caracteres>…`).
Servicio: PharmaSoft en el PC del equipo, zona con cobertura de Windows (`http://192.168.137.1:8080/api/v1/`).

> Estado: **pendiente de ejecutar**. Ninguna fila tiene resultado todavía; se llena con lo que se observe en cada dispositivo, con su fecha real.

## Dispositivos

| N.º | Dispositivo | Plataforma | Build instalado | Pasos ejecutados | Resultado | Responsable y fecha |
|---|---|---|---|---|---|---|
| 1 | <Marca Modelo, Android X (API Y)> | Android | `…_Android.apk` 1.0.0 | 1–8 | <Correcto / Con observaciones (D-0x) / Fallido (D-0x)> | <Nombre>, <fecha> |
| 2 | <Marca Modelo distinta, Android X> | Android | `…_Android.apk` 1.0.0 | 1–8 | <…> | <Nombre>, <fecha> |
| 3 | (iOS — se completa en la GAA) | iOS | — | — | — | — |

## Recorrido de 8 pasos

| N.º | Paso | Cómo provocarlo en PharmaMobil | Qué se observa |
|---|---|---|---|
| 1 | Instalación desde el archivo firmado | Instalar el `.apk` sin Android Studio | Instala; nombre «PharmaMobil» |
| 2 | Login correcto e incorrecto | `admin` con clave mala → luego buena; después apagar la zona con cobertura y probar | «Usuario o contraseña incorrectos.» vs «Sin conexión con el servidor…» |
| 3 | CRUD completo con un rechazo | Alta, edición, baja; alta con nombre repetido | El 409 aparece bajo el campo **Nombre** |
| 4 | Operación sin privilegio | Entrar como `vendedor` | Acciones de escritura ocultas; la sesión no se cierra |
| 5 | Modo avión | Modo avión → abrir Productos → crear uno | «Pendiente de envío» y «1 cambio por enviar» |
| 6 | Recuperar la red | Quitar el modo avión, **sin** pulsar ⟳ | Sube solo (H7), sin duplicar |
| 7 | Permiso: conceder, denegar, para siempre | Pestaña *Bajo Stock* → *Activar avisos* | Los tres estados; el tercero ofrece *Abrir Ajustes* |
| 8 | Cerrar sesión y reabrir | Con un pendiente → *Cerrar sesión* → *Salir y descartar* → reabrir | Pide login; la lista ya no tiene datos del usuario anterior (H8) |

## Defectos

| Id | Dispositivo y versión | Pasos para reproducir | Esperado | Obtenido | Severidad | Estado |
|---|---|---|---|---|---|---|
| — | *Ninguno registrado todavía: no se ha probado en dispositivos reales.* | | | | | |
