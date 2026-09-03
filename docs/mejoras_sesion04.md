# Guía de mejoras — PharmaMobil (Sesión 4, Retos 01 y 02)

Revisión del código en `main` (commit `7d78866`) contra la guía práctica y autónoma de la Sesión 4.

> **Actualización 2026-09-02:** todos los ítems P1 y P2, y la mayoría de los P3, ya se aplicaron y
> se verificaron con `./gradlew :shared:testAndroidHostTest` (31 tests en verde) y en el emulador
> (ver [docs/evidencias_sesion04.md](evidencias_sesion04.md)). Cada sección abajo indica su estado.

## 1. Estado actual: qué ya está cumplido

| Requisito de la guía | Estado | Dónde |
|---|---|---|
| 4 destinos (Inicio, Productos, Clientes, Pedidos) | Cumplido | `presentation/navigation/Destino.kt` |
| Destinos tipados (sin strings dispersos) | Cumplido | `sealed class Destino` con `ruta`, `titulo`, `icono` |
| Estado único de navegación + `when` | Cumplido | `App.kt:24`, `App.kt:42` |
| Drawer con marca y selección visual | Cumplido | `PharmaDrawer.kt` |
| `Scaffold` + `TopAppBar` con título dinámico | Cumplido | `PharmaNavigationAdaptativa.kt:152` |
| Reutilización de `ProductoScreen` sin duplicar | Cumplido | `ProductosMainScreen.kt:171` |
| Tabs Activos / Inactivos / Bajo stock | Cumplido | `ProductosMainScreen.kt:77` |
| Regla estricta `stock <= 5` | Cumplido (unificada en el dominio) | `domain/query/ConsultasProductos.kt` |
| Datos simulados exactos de la tabla 8.2 | Cumplido | `ProductoRepositorySimulado.kt:15` |
| Modo claro / oscuro global | Cumplido | `Theme.kt`, toggle en `TopAppBar` |
| Recurso compartido consumido en Inicio | Cumplido | `InicioScreen.kt:54` (`pharmamobil_logo`) |
| Navegación adaptativa por breakpoints | Cumplido | `PharmaNavigationAdaptativa.kt:51` (600dp / 840dp) |
| Pruebas unitarias del formulario | Cumplido | 25 `@Test` en `commonTest` |

Fuente única de verdad correcta: `ProductoRepositorySimulado` expone `StateFlow`, `App` lo observa con
`collectAsState` y las pantallas son *stateless*. Esa es la decisión más acertada del avance.

---

## 2. Mejoras por prioridad

### ✅ P1 — Dos definiciones distintas de "bajo stock" (Hecho)

`ProductosMainScreen.kt:71` usa `it.stock <= 5`, pero `domain/query/ConsultasProductos.kt:16` define
`conStockBajo(minimo: Int = 25) = filter { it.stock < minimo }`. Son reglas contradictorias y la de dominio
además usa `<` en lugar de `<=`.

Acción: dejar **una sola** regla en el dominio y consumirla desde la UI.

```kotlin
// domain/query/ConsultasProductos.kt
const val UMBRAL_BAJO_STOCK = 5

fun List<Producto>.activos(): List<Producto> = filter { it.activo }
fun List<Producto>.inactivos(): List<Producto> = filter { !it.activo }
fun List<Producto>.bajoStock(umbral: Int = UMBRAL_BAJO_STOCK): List<Producto> = filter { it.stock <= umbral }
```

Beneficio doble: elimina la contradicción y hace **testeable** el filtrado, que hoy vive dentro del
composable y por eso no tiene ni una prueba. Añadir en `commonTest`:

```kotlin
@Test fun amoxicilina_con_stock_5_es_bajo_stock() { /* frontera de la regla */ }
@Test fun loratadina_inactiva_con_stock_0_aparece_en_inactivos_y_en_bajo_stock() { }
```

Aplicado en `domain/query/ConsultasProductos.kt` (`UMBRAL_BAJO_STOCK`, `activos()`, `inactivos()`,
`bajoStock()`) y consumido desde `App.kt` y `ProductosMainScreen.kt`. Cubierto por
`ConsultasProductosTest` (5 casos nuevos, incluida la frontera `stock == 5`).

### ✅ P1 — El `ColorScheme` está incompleto y la identidad corporativa se rompe (Hecho)

`Theme.kt` define 12 roles, pero el código consume roles que **no** están definidos y por lo tanto caen en
los valores baseline de Material 3 (morados/grises genéricos):

- `onSurfaceVariant` → `InicioScreen.kt:70`, `PharmaDrawer.kt:45`, `ProductosMainScreen.kt:97`
- `surfaceVariant` → `InicioScreen.kt:110`, `ProductosMainScreen.kt:198`
- `errorContainer` / `onErrorContainer` → `InicioScreen.kt:143`, `InicioScreen.kt:159`

Acción: completar `surfaceVariant`, `onSurfaceVariant`, `outline`, `errorContainer`, `onErrorContainer`,
`secondaryContainer`, `onSecondaryContainer` en ambos esquemas. Es exactamente el criterio "Theming" de la
rúbrica: hoy la app se ve corporativa a medias.

Completado en `Color.kt` y `Theme.kt` para ambos esquemas. Verificado visualmente en claro y oscuro
(ver capturas 03, 04, 07 y 08 en [evidencias_sesion04.md](evidencias_sesion04.md)): las tarjetas de
"Bajo Stock" ya usan `errorContainer` real y no el morado por defecto de M3.

### ✅ P1 — Falta `Typography` y `Shapes` (punto 8.4 de la guía) (Hecho)

`MaterialTheme(colorScheme = ...)` solo propaga color. La guía pide centralizar **colores, tipografías y
formas**. Crear `Type.kt` y `Shape.kt` y pasarlos:

```kotlin
MaterialTheme(
    colorScheme = colorScheme,
    typography = PharmaTypography,
    shapes = PharmaShapes,
    content = content
)
```

Con eso además desaparecen los `fontWeight = FontWeight.Bold` sueltos repartidos por `InicioScreen` y
`ProductosMainScreen` (mismo problema que los hex sueltos, pero en tipografía).

Creados `presentation/theme/Type.kt` (`PharmaTypography`) y `presentation/theme/Shape.kt`
(`PharmaShapes`), propagados en `PharmaMobilTheme`.

### ✅ P2 — El estado se pierde al rotar la pantalla (Hecho)

`destinoActual` (`App.kt:24`), `modoOscuro` (`App.kt:25`) y `tabSeleccionada`
(`ProductosMainScreen.kt:64`) usan `remember`. Al rotar el dispositivo Android la app vuelve a Inicio y a
modo claro. Esto contradice la prueba 1 ("la navegación no debe perder estados inesperadamente") y se ve
mal en las evidencias.

Acción: `rememberSaveable` en los tres. Para `Destino`, guardar la `ruta` (String) y resolver el objeto, o
mover el estado a un `ViewModel` — nota: `androidx.lifecycle.viewmodelCompose` **ya está declarado** en
`shared/build.gradle.kts` y no se usa. Ese es el siguiente paso natural de arquitectura.

Aplicado exactamente así: `App.kt` guarda `rutaDestinoActual` (String) y `modoOscuro` con
`rememberSaveable`; `ProductosMainScreen.kt` guarda `tabSeleccionada` con `rememberSaveable`. La
migración a `ViewModel` queda pendiente como mejora de arquitectura, no de esta sesión.

### ✅ P2 — Código muerto: `PharmaAppScaffold.kt` (Hecho)

El archivo completo (77 líneas, con su propio `ModalNavigationDrawer` + `TopAppBar`) no lo referencia nadie;
`App.kt` usa `PharmaNavigationAdaptativa`. Es lógica de navegación duplicada que se va a desincronizar.

Acción: borrarlo.

Archivo eliminado.

### ✅ P2 — `ModalDrawerSheet` anidado dentro de `PermanentDrawerSheet` (Hecho)

En escritorio, `PharmaNavigationAdaptativa.kt:55` envuelve `PharmaDrawerContent` en un
`PermanentDrawerSheet`, pero `PharmaDrawerContent` ya abre su propio `ModalDrawerSheet`
(`PharmaDrawer.kt:28`). Resultado: dos superficies, doble elevación y anchos duplicados.

Acción: extraer el contenido sin el sheet.

```kotlin
@Composable
fun PharmaDrawerContenidoInterno(destinoActual: Destino, onSeleccionarDestino: (Destino) -> Unit) {
    // marca + items de navegación
}

@Composable
fun PharmaDrawerContent(destinoActual: Destino, onSeleccionarDestino: (Destino) -> Unit) =
    ModalDrawerSheet { PharmaDrawerContenidoInterno(destinoActual, onSeleccionarDestino) }
```

y en la rama de escritorio usar `PermanentDrawerSheet { PharmaDrawerContenidoInterno(...) }`.

Aplicado tal cual: `PharmaDrawerContenidoInterno` vive en `PharmaDrawer.kt`, y
`PharmaNavigationAdaptativa.kt` la usa desnuda dentro del `PermanentDrawerSheet` de escritorio.

### ✅ P2 — El `delay(1200)` para cerrar el diálogo (Hecho)

`ProductosMainScreen.kt:176` lanza una corrutina en el `rememberCoroutineScope` para esperar 1.2 s y luego
cerrar el diálogo. Funciona, pero mezcla temporización con navegación y es frágil si el usuario cierra
antes. Lo idiomático es un `SnackbarHost` en el `Scaffold`, o cerrar de inmediato y mostrar el mensaje de
confirmación fuera del diálogo.

Aplicado con `SnackbarHost` global: `App.kt` hostea un único `SnackbarHostState` y lo pasa a
`PharmaNavigationAdaptativa`, que lo conecta al `Scaffold` compartido (`ScaffoldTopBarAdaptativo`) usado
por los tres factores de forma. Al registrar un producto, `App.kt` lanza
`snackbarHostState.showSnackbar(MensajesProducto.REGISTRO_EXITOSO)` y el diálogo de
`ProductosMainScreen` se cierra de inmediato, sin `delay`. Verificado en el emulador: el diálogo
desaparece al instante y el snackbar "Producto registrado correctamente." aparece flotando sobre la
lista de Productos.

### P3 — Detalles visibles en las evidencias

- ✅ **Precio sin formato** — resuelto con `Double.aSoles()` en `presentation/theme/Formato.kt`,
  usado en `ProductosMainScreen.kt` y `ProductoScreen.kt`. Verificado: `S/ 15.50`, `S/ 9.90`.
- ✅ **`app_name` "PharmaMobile"** — cambiado a `PharmaMobil` en `strings.xml`. De paso se actualizó
  `rootProject.name` en `settings.gradle.kts` y `.idea/.name` (ver Anexo, sección 4).
- ✅ **README desactualizado** — reescrito: refleja el estado real (Sesión 4, sin backend/REST
  todavía) en vez de un stack aspiracional.
- ✅ **No se podían registrar productos inactivos** — se agregó un `Switch` "Producto activo" en
  `ProductoScreen.kt` (default `true`), y `validarYCrearProducto` ahora lo recibe. Verificado
  registrando "Vitamina C" con el switch apagado: apareció correctamente en Inactivos.
- ⬜ **`key = { "${it.id}_${it.nombre}" }`** (`ProductosMainScreen.kt`) — pendiente, es cosmético y de
  muy bajo riesgo; se puede simplificar a `key = { it.id }` cuando se toque ese archivo de nuevo.

### ⬜ P3 — La adaptabilidad no se puede *demostrar* en escritorio (Pendiente)

`settings.gradle.kts` solo incluye `:androidApp` y `:shared` (targets iOS). No hay target JVM/desktop, así
que la prueba 11 ("ejecutar en teléfono, tablet y escritorio") no se puede evidenciar de verdad.

Dos salidas, en orden de esfuerzo:

1. Usar el **Resizable Emulator** de Android Studio y capturar los tres anchos (teléfono / tablet /
   desplegado). Cubre la rúbrica si se justifica por escrito.
2. Añadir un módulo `desktopApp` con target JVM y `application { ... }` de Compose Multiplatform. Es la
   evidencia fuerte y reutiliza el 100 % de `shared`.

Sigue pendiente: no había un AVD de tablet ni el Resizable Emulator disponibles en este entorno para
capturar los otros dos factores de forma. Documentado como pendiente en
[evidencias_sesion04.md](evidencias_sesion04.md) §4.

### ✅ P3 — Entregable 2 ausente (Hecho)

El repositorio no tiene el "Documento de evidencias y decisiones" (capturas, matriz de pruebas de la
sección 9, decisiones de diseño adaptativo, registro de incidencias de la sección 11). Es un criterio
completo de la rúbrica. Crear `docs/evidencias_sesion04.md` con las 12 filas de la matriz llenas y las
capturas en `docs/img/`.

Ahí mismo debe quedar **documentada explícitamente** la decisión que pide el punto 8.3: *Bajo stock es una
vista transversal del nivel de inventario, por lo que un producto puede aparecer simultáneamente en Activos
y en Bajo stock (Amoxicilina, Diclofenaco) o en Inactivos y en Bajo stock (Loratadina).* El código ya se
comporta así, pero no está escrito en ningún lado.

Creado [docs/evidencias_sesion04.md](evidencias_sesion04.md) con la matriz de 12 pruebas (10 ✅, 1 ⚠️
pendiente por falta de AVD de tablet, 1 vía test unitario), 8 capturas reales en `docs/img/`, la
decisión de la vista transversal y el registro de las 4 incidencias corregidas en este pase.

---

## 3. Orden sugerido de trabajo

1. ✅ Unificar la regla de bajo stock en el dominio + pruebas del filtrado.
2. ✅ Completar `ColorScheme`, `Typography` y `Shapes`.
3. ✅ `rememberSaveable` en los tres estados.
4. ✅ Borrar `PharmaAppScaffold.kt` y arreglar el sheet anidado.
5. ✅ Formato de precio, `app_name`, `Switch` de activo, README.
6. ✅ Evidencias: matriz de pruebas + capturas + decisión de la vista transversal.
7. ⬜ Opcional pero fuerte: módulo `desktopApp` — sigue pendiente, ver nota abajo.
8. ✅ `SnackbarHost` global en vez del `delay(1200)` para cerrar el diálogo de registro.
9. ⬜ Evidencia de tablet/escritorio con AVD o Resizable Emulator.

Sobre el punto 7 (`desktopApp`): es un cambio de arquitectura (nuevo módulo Gradle, target JVM,
`application {}`) más grande que el resto de este pase y con más riesgo de romper la build sin poder
probarlo interactivamente aquí. Si quieres que lo arme, dímelo en un mensaje aparte y lo hago con
tiempo para validarlo bien en vez de meterlo de pasada.

---

## 4. Anexo: por qué la carpeta raíz sigue llamándose "PharmaMobile"

Hay **tres identidades distintas** que se confunden con el nombre del proyecto:

| Identidad | Dónde vive | Valor actual |
|---|---|---|
| Carpeta en disco | `C:\Users\jesam\AndroidStudioProjects\PharmaMobil` | ✅ ya renombrada |
| Nombre del proyecto Gradle / IDE | `settings.gradle.kts:1` + `.idea/.name` | ✅ ya en `PharmaMobil` |
| Paquete Kotlin y `applicationId` | `pe.edu.upeu.pharmamobile` | sin cambiar (a propósito, ver nota abajo) |

Android Studio no deja renombrar la carpeta raíz desde el propio IDE por dos motivos:

1. Esa carpeta **es** el proyecto abierto. El IDE mantiene bloqueados `.idea/`, `.gradle/` y el daemon de
   Gradle apunta a esa ruta; renombrarla en caliente rompería el proyecto abierto. Por eso el `Rename` del
   nodo raíz solo ofrece cambiar `rootProject.name`, no el directorio. La carpeta se renombra con el
   proyecto **cerrado**, desde el explorador de archivos (que es lo que ya se hizo).
2. Lo que se ve en el árbol del proyecto **no es el nombre de la carpeta**, es `rootProject.name` de
   `settings.gradle.kts`. Por eso, aun con la carpeta renombrada, Android Studio sigue mostrando
   `PharmaMobile`.

Y el paquete `pe.edu.upeu.pharmamobile` es una tercera cosa, totalmente independiente de la carpeta raíz:
está en el `package` de los ~40 archivos `.kt`, en `namespace` y `applicationId` de
`androidApp/build.gradle.kts`, en `namespace` y `packageOfResClass` de `shared/build.gradle.kts`, en el
`AndroidManifest`, en `Config.xcconfig` y en `project.pbxproj`. Renombrar la carpeta jamás lo iba a tocar.

### Si se quiere completar el renombrado

✅ Ya aplicado: `rootProject.name = "PharmaMobil"` en `settings.gradle.kts`, `app_name` en
`strings.xml` y el título del README. El IDE, el APK y las capturas ya dicen PharmaMobil.

Cambio caro (opcional, mejor después de entregar la sesión, no aplicado): renombrar el paquete a
`pe.edu.upeu.pharmamobil` con *Refactor → Rename* sobre el paquete en Android Studio, y actualizar a mano
`namespace`, `applicationId`, `packageOfResClass` y las referencias de iOS. Ojo: cambiar el `applicationId`
convierte la app en **otra aplicación distinta** para Android, así que hay que desinstalar la anterior del
emulador. Por eso no es un cambio que convenga hacer en medio de la entrega.

Nota práctica: dejar el paquete en `pharmamobile` y el proyecto en `PharmaMobil` no es incoherente ni penaliza
en la rúbrica; el paquete es un identificador técnico y lo habitual es congelarlo temprano.
