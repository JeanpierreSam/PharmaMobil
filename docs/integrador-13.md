# Proyecto integrador · Sesión 13

Recorrido del 04-11-2026, Android, rama `feature/integrador-13`.

> Nota de método: la columna «Qué se observó» sale de la revisión del código de las sesiones 8 a 12.
> Falta confirmarla en el emulador (API 36) y adjuntar las capturas.

## Recorrido (paso 1 de la guía práctica)

| N.º | Momento | Qué se observó |
|---|---|---|
| 1 | Primera apertura, sin sesión | Pantalla de login; la base `pharma.db` existe con las tablas vacías. Se puede salir (no hay callejón). |
| 2 | Credenciales incorrectas | «Usuario o contraseña incorrectos»; con PharmaSoft apagado el mensaje es distinto (sin conexión). Bien distinguido. |
| 3 | Listado con la base vacía | Texto propio «Todavía no hay productos registrados» / «Aún no hay datos guardados…». No parece error. |
| 4 | Creación en modo avión | Se guarda con id negativo y etiqueta «Pendiente de envío». La barra no dice cuántos cambios faltan. |
| 5 | Sincronización al recuperar la señal | No es automática: hay que pulsar ⟳. Llega sin duplicarse (confirmarSubida, S11). |
| 6 | Error de validación del servidor | Con red el 400 llega bajo su campo (S8). Pero un producto creado **sin red** que luego PharmaSoft rechaza (409 nombre repetido) queda «Pendiente de envío» para siempre y se reenvía en cada sincronización sin avisar. |
| 7 | Token vencido a mitad de un formulario | Al guardar llega el 401, la app vuelve al login y se pierde lo escrito. |
| 8 | Cierre de sesión | Se borran el token y el perfil, pero el catálogo y los pendientes quedan en `pharma.db`. |

## Huecos (estado al 04-11-2026)

| N.º | Hueco detectado | Gravedad | Decisión | Fecha de cierre |
|---|---|---|---|---|
| H1 | Un pendiente que PharmaSoft rechaza queda atascado para siempre y nadie lo dice | Alta | Cerrado hoy (pasos 2–4) | 04-11-2026 |
| H2 | Un 503 o tiempo agotado se reintenta sin límite ni espera | Media | Cerrado hoy (paso 3) | 04-11-2026 |
| H3 | La barra no muestra cuántos cambios faltan por enviar | Media | Cerrado hoy (paso 4) | 04-11-2026 |
| H4 | La app no usa ningún permiso y la S14 necesita notificaciones (Android 13+) | Media | Sesión 13 (paso 5) | — |
| H5 | Los fallos ocurridos en el teléfono no llegan a ningún lado | Media | Sesión 13 (paso 6) | — |
| H6 | Token vencido a mitad del formulario: se pierde lo escrito | Alta | Sesión 14 | 10-11-2026 |
| H7 | Al recuperar la señal no se sincroniza sola | Media | Sesión 14 | 10-11-2026 |
| H8 | El cierre de sesión deja catálogo y pendientes del usuario anterior | Media | Sesión 14 | 10-11-2026 |
