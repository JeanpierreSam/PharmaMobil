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

## Ficha de integración (04-11-2026)

| N.º | Hueco detectado | Gravedad | Decisión | Fecha de cierre |
|---|---|---|---|---|
| H1 | Un pendiente que PharmaSoft rechaza queda atascado para siempre y nadie lo dice | Alta | Cerrado hoy: intentos, fallidas visibles, Revisar | 04-11-2026 |
| H2 | Un 503 o tiempo agotado se reintenta sin límite ni espera | Media | Cerrado hoy: 5 intentos, espera 1-2-4-8 s | 04-11-2026 |
| H3 | La barra no muestra cuántos cambios faltan por enviar | Media | Cerrado hoy | 04-11-2026 |
| H4 | La app no usa permisos; la S14 necesita notificaciones (Android 13+) | Media | Cerrado hoy: GestorPermisos + avisos de stock | 04-11-2026 |
| H5 | Los fallos en el teléfono no llegan a ningún lado | Media | Cerrado hoy: Crashlytics y Analytics detrás de Telemetria | 04-11-2026 |
| H6 | Token vencido a mitad de una edición: se pierde lo escrito (el alta sí queda pendiente) | Alta | Sesión 14: guardar borrador de la edición antes del login y restaurarlo | 10-11-2026 |
| H7 | Al recuperar la señal no se sincroniza sola | Media | Sesión 14: sincronizar al volver la conectividad | 10-11-2026 |
| H8 | El cierre de sesión deja catálogo y pendientes del usuario anterior | Media | Sesión 14: advertir si hay pendientes y limpiar la base al salir | 10-11-2026 |
| H9 | Reintentar un alta cuya respuesta se perdió puede repetirla | Baja | Descartado: el nombre único de PharmaSoft responde 409 y la fila queda visible para descartarla; una clave de operación exige cambiar el backend del curso | — |
| H10 | Editar o dar de baja un producto sincronizado exige red | Baja | Descartado: fuera del alcance (S11); exige una cola de operaciones y el usuario ve el aviso sin perder datos | — |
| H11 | Al volver de Ajustes el permiso activado no se refleja | Media | Cerrado hoy: LifecycleResumeEffect | 04-11-2026 |
| H12 | Cloud Messaging no está integrado | Media | Sesión 14: los avisos remotos son el tema de esa sesión | 10-11-2026 |
| H13 | iOS no se ha ejecutado (sin Mac) | Media | Descartado esta semana: el código compila a klib en Windows y el estado está en el README; se prueba cuando haya Mac | — |
