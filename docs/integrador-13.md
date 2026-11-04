# Proyecto integrador · Sesión 13

Recorrido del 04-11-2026 en el emulador Pixel_8 (Android 14, API 34), rama `feature/integrador-13`,
contra PharmaSoft en local. El paso 1 se había escrito revisando el código; esta versión registra lo
que de verdad se vio con la app ya integrada.

## Recorrido (paso 1 de la guía práctica)

| N.º | Momento | Qué se observó |
|---|---|---|
| 1 | Primera apertura, sin sesión | Pantalla de login. `pharma.db` **todavía no existe**: el driver la crea con la primera consulta, tras iniciar sesión. |
| 2 | Credenciales incorrectas | «Usuario o contraseña incorrectos.» Con PharmaSoft apagado el mensaje es otro: «Sin conexión con el servidor…». Bien distinguido. |
| 3 | Listado con la base vacía | «Aún no hay datos guardados en este dispositivo…» y «Nunca sincronizado: los datos pueden estar incompletos». No parece error. |
| 4 | Creación en modo avión | Id negativo y etiqueta «Pendiente de envío»; la barra dice «· 1 cambio por enviar». |
| 5 | Sincronización al recuperar la señal | No es automática: hay que pulsar ⟳. Llega con id real y sin duplicarse. |
| 6 | Error de validación del servidor | Nombre de una letra: 400 con el mensaje bajo su campo. Un alta sin red que PharmaSoft rechaza (409) queda «No se pudo enviar», con *Revisar*. |
| 7 | Token vencido a mitad de un formulario | **Alta:** vuelve al login y el producto queda pendiente (id -1); se envía tras entrar, no se pierde. **Edición:** vuelve al login y el cambio se pierde (stock 77 escrito, sigue en 40). |
| 8 | Cierre de sesión | Se borran el token y el perfil; las 14 filas siguen en `pharma.db`. |

Migración comprobada en el emulador: una base en versión 1 con 15 filas pasó a la versión 2 al abrirse
tras el login, con las 15 filas intactas, y su pendiente se envió con id real.

## Bitácora de permisos: POST_NOTIFICATIONS

| Estado | Qué ve el usuario | Cómo recupera la función |
|---|---|---|
| Concedido | «Avisos activados» y *Enviar aviso ahora*; la notificación llega | No hace falta |
| Denegado | La explicación y *Activar avisos* otra vez | Vuelve a pulsar *Activar avisos* |
| Denegado para siempre | «Los avisos están bloqueados…» y *Abrir Ajustes*; el sistema ya no muestra el diálogo | *Abrir Ajustes* → Notifications → activar |

El sistema mostró el diálogo dos veces: «Allow PharmaMobil to send you notifications?».

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
