# PharmaMobil

Aplicación multiplataforma para la gestión integral de clientes,
productos, pedidos e inventario farmacéutico.

## Stack Tecnológico
- Kotlin Multiplatform (KMP)
- Compose Multiplatform
- Material 3 (theming corporativo, modo claro/oscuro)

## Estado actual (Sesión 9 — Capacidades nativas con expect/actual)
- Módulo de Productos en capas Clean Architecture + MVVM con Koin (Sesión 5).
- CRUD completo de productos (listar, obtener, crear, actualizar y dar de baja)
  contra el backend del curso **PharmaSoft**, con Ktor Client; ver
  [Conexión con PharmaSoft](#conexión-con-pharmasoft).
- Navegación principal con `ModalNavigationDrawer` + `Scaffold` + `TopAppBar`,
  con 4 destinos tipados (Inicio, Productos, Clientes y Pedidos) y navegación
  adaptativa por factor de forma (Sesión 4).
- Identidad visual centralizada en `PharmaMobilTheme` con modo claro/oscuro.
- Precio en soles con el formato de cada plataforma y botón **Compartir** en el
  detalle del producto; ver [Capacidades nativas](#capacidades-nativas).
- Pantalla **Acerca de** con sistema, versión y modelo del dispositivo; ver
  [Código específico de plataforma](#código-específico-de-plataforma).

## Conexión con PharmaSoft

**Backend:** [PharmaSoft](https://github.com/dreyna/pharmaSoft), API REST en
Spring Boot 3 + Oracle, levantada en local en el puerto 8080. Recurso
`/api/v1/productos`; salud en `GET /api/health`; documentación en
`/swagger-ui.html`.

**URL base por plataforma** (`platformModule`, calificador `URL_BASE`):

| Plataforma | URL base | Motivo |
|---|---|---|
| Android (emulador) | `http://10.0.2.2:8080/api/v1/` | El emulador ve al PC anfitrión como 10.0.2.2 |
| iOS (simulador) | `http://localhost:8080/api/v1/` | El simulador comparte la red del Mac |

El backend es HTTP sin TLS: Android lo permite solo hacia `10.0.2.2`
(`res/xml/network_security_config.xml`) e iOS con `NSAllowsLocalNetworking`
en `Info.plist`.

**Endpoints consumidos** (`data/remote/ProductoApi.kt`):

| Operación | Petición | Éxito |
|---|---|---|
| Listar | `GET /productos?pagina=0&tamanio=20` | 200 · `PaginaResponseDto` (lista en `contenido`) |
| Obtener | `GET /productos/{id}` (al pulsar Editar o abrir el detalle) | 200 |
| Crear | `POST /productos` con `ProductoRequestDto` | 201 |
| Actualizar | `PUT /productos/{id}` con los 5 campos (no hay PATCH) | 200 |
| Eliminar | `DELETE /productos/{id}` | 204 sin cuerpo (no se llama a `body()`) |

**DTO y mapeo:** `ProductoRequestDto` (`nombre`, `precio`, `stock`, `estado`,
`categoriaId`), `ProductoResponseDto` (además `id` y `categoriaNombre`; las
fechas se ignoran con `ignoreUnknownKeys`), `PaginaResponseDto<T>` y
`ErrorResponseDto`. El mapper traduce `estado` ↔ `Producto.activo`. El dominio
aún no maneja categorías: todos los productos se envían con
`CATEGORIA_POR_DEFECTO = 1L` («Analgésicos», creada en el backend).

**Baja lógica:** en PharmaSoft el DELETE no borra la fila, pone
`estado = false`. El producto pasa a la pestaña **Inactivos** y puede
reactivarse editándolo. Un segundo DELETE sobre el mismo id responde 409.

**Cliente e inyección:** `crearHttpClient(engine, urlBase)` con
`ContentNegotiation`, `Logging` (prefijo `Ktor:` en Logcat), `HttpTimeout`
(15 s / 10 s) y `DefaultRequest`. El motor lo aporta cada plataforma (OkHttp /
Darwin). `dataModule` registra un único `HttpClient`, `ProductoApi` y
`ProductoRepositorioRest`; `ProductoRepositorioEnMemoria` se conserva como
alternativa para pruebas.

**Versión de Ktor:** 3.6.0, que trae OkHttp 5.5.0 y exige `compileSdk` 37
(`targetSdk` sigue en 36). AGP 9.0.1 avisa que está probado hasta la 36.1; el
build, las pruebas y la app funcionan.

### Levantar el backend en local
```powershell
docker start oracle-local        # Oracle Free en el puerto 1522 (usuario PHARMADB en FREEPDB1)
cd <ruta>\pharmaSoft
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"   # JDK 21
.\mvnw.cmd spring-boot:run      # Flyway crea el esquema en el primer arranque
```

## Manejo de errores

Las excepciones de Ktor se traducen **en un único punto**,
`data/remote/EjecutarLlamada.kt`, a un tipo del dominio: `ErrorApi`
(`domain/error`). Ninguna clase de `presentation` importa `io.ktor`, y el `when`
de `mensajeDe` es exhaustivo: agregar un caso nuevo (por ejemplo, el 401 de la
Sesión 10) obliga a darle mensaje.

| Origen | `ErrorApi` | Qué ve el usuario |
|---|---|---|
| 400 con `validationErrors` | `Validacion(porCampo)` | Cada mensaje del servidor **debajo de su campo** (nombre, precio, stock) |
| 404 | `NoEncontrado` | «No se encontró el recurso solicitado.» |
| 409 (`ReglaNegocioException`) | `Conflicto(mensaje)` | El mensaje del servidor, p. ej. «Ya existe un producto con el nombre …» |
| 5xx, JSON que no encaja con el DTO | `Servidor` | «El servidor no está disponible. Inténtalo más tarde.» |
| Backend caído, sin red (`IOException`) | `SinConexion` | «Sin conexión con el servidor. Revisa tu red.» |
| `HttpRequestTimeoutException` | `TiempoAgotado` | «El servidor tardó demasiado en responder.» |

**Dónde se muestra.** Si falla la carga del listado, la pantalla pasa a
`Fase.Error` con el botón **Reintentar**. Si falla una operación (crear,
actualizar, dar de baja), la lista no desaparece: el estado va a
`Operacion.Fallida`. Con el formulario abierto, el mensaje aparece dentro del
formulario; si no, en un Snackbar. Los errores de validación nunca llevan a
`Fase.Error`.

**Validación local y del servidor.** La app rechaza antes de enviar lo que el
modelo `Producto` no admite (campo vacío, no numérico, precio ≤ 0, stock
negativo), con los mismos textos que PharmaSoft. Las reglas propias del backend
(nombre de 3 a 150 caracteres, precio mínimo 0.01, nombre único) llegan como
`Validacion` o `Conflicto`.

**Particularidades de PharmaSoft.** El DELETE es una baja lógica
(`estado = false`): un segundo DELETE sobre el mismo producto responde **409**
(«…ya se encuentra inactivo»), no 404. Un PUT debe llevar los cinco campos; si
falta alguno, responde 400.

**Cancelación.** `ejecutarLlamada` y `resultadoDe` (casos de uso) relanzan la
`CancellationException` en lugar de convertirla en error. Cuando la Activity se
destruye, `viewModelScope` se cancela y la operación en curso termina sin
mostrar nada ni enviar la petición pendiente. Cambiar de sección en el menú no
cancela: el ViewModel vive mientras vive la Activity y la operación termina
normalmente. Esto lo cubre la prueba `CancelacionUseCaseTest`.

## Capacidades nativas

La interfaz y el dominio viven en `commonMain`; solo baja a cada plataforma lo
que depende del sistema operativo. Se usan dos estrategias:

- **expect/actual** para utilidades sin estado: el compilador exige el `actual`
  de cada plataforma (si falta uno, el target no compila).
- **Interfaz en `domain` + inyección** cuando la implementación necesita algo
  que `commonMain` no conoce (un `Context`, un controlador de vista). Cada
  plataforma registra la suya dentro del `expect val platformModule`, y en las
  pruebas se sustituye por un doble (`FakeCompartidor`).

| Capacidad | commonMain | androidMain | iosMain |
|---|---|---|---|
| Formato de moneda | `platform/Formato.kt` — `expect fun formatearSoles(valor: Double): String` | `platform/Formato.android.kt` — `NumberFormat` con `es-PE` | `platform/Formato.ios.kt` — `NSNumberFormatter` con `es_PE` |
| Compartir producto | `domain/platform/Compartidor.kt` — `interface Compartidor` | `platform/CompartidorAndroid.kt` — `Intent.ACTION_SEND` + chooser | `platform/CompartidorIos.kt` — `UIActivityViewController` |
| Módulo de inyección | `di/AppModule.kt` — `expect val platformModule` | `di/PlatformModule.android.kt` — `CompartidorAndroid(androidContext())` | `di/PlatformModule.ios.kt` — `CompartidorIos()` sin contexto |

El formato se aplica en `presentation/producto/ProductoUi.kt` (`toUi()`), no en
el dominio ni en los composables. El texto que se comparte se arma una sola vez
en `domain/usecase/TextoParaCompartir.kt`. Ninguna clase de `commonMain` importa
`android.*` ni `platform.*`.

**Detalles por plataforma.** En Android el precio sale como `S/ 4.50` con un
espacio no separable (U+00A0) entre el símbolo y el número. El `Context` que
entrega Koin es el de la aplicación, por eso el selector se lanza con
`FLAG_ACTIVITY_NEW_TASK`. En iOS la hoja se presenta sobre el controlador
visible de la ventana activa (`UIWindowScene.keyWindow`; `UIApplication.keyWindow`
está deprecado) y en iPad se ancla como popover.

**Kotlin visto desde Swift.** `iosApp` importa el framework como `import Shared`.
Las funciones de nivel superior de `KoinInit.kt` y `MainViewController.kt` llegan
como `KoinInitKt` y `MainViewControllerKt`, y `initKoinIos()` se invoca como
`doInitKoinIos()` porque Swift reserva los nombres `init*` para constructores.

## Código específico de plataforma

Inventario de todo lo que baja a `androidMain` / `iosMain`. Las rutas son
relativas a `shared/src/<sourceSet>/kotlin/pe/edu/upeu/pharmamobile/`.

| Capacidad | Declaración en commonMain | Android (androidMain) | iOS (iosMain) |
|---|---|---|---|
| Formato de moneda | `expect fun formatearSoles(valor: Double): String` — `platform/Formato.kt` | `platform/Formato.android.kt` · `NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-PE"))` | `platform/Formato.ios.kt` · `NSNumberFormatter` + `NSLocale("es_PE")` |
| Compartir producto | `interface Compartidor` — `domain/platform/Compartidor.kt` | `platform/CompartidorAndroid.kt` · `Intent.ACTION_SEND` + `Intent.createChooser` | `platform/CompartidorIos.kt` · `UIActivityViewController` |
| Información del dispositivo | `expect class InfoDispositivo()` — `platform/InfoDispositivo.kt` | `platform/InfoDispositivo.android.kt` · `Build.VERSION.RELEASE`, `Build.MANUFACTURER`, `Build.MODEL` | `platform/InfoDispositivo.ios.kt` · `UIDevice.currentDevice` |
| Módulo de inyección | `expect val platformModule: Module` — `di/AppModule.kt` | `di/PlatformModule.android.kt` · motor `OkHttp`, URL `10.0.2.2`, `CompartidorAndroid(androidContext())` | `di/PlatformModule.ios.kt` · motor `Darwin`, URL `localhost`, `CompartidorIos()` |
| Barra de estado | `expect fun ConfiguracionBarraEstado(modoOscuro: Boolean)` — `presentation/theme/BarraEstado.kt` | `presentation/theme/BarraEstado.android.kt` · `WindowInsetsController` | `presentation/theme/BarraEstado.ios.kt` · vacío: la gestiona el sistema |
| Plataforma (plantilla) | `expect fun getPlatform(): Platform` — `Platform.kt` | `Platform.android.kt` · `Build.VERSION.SDK_INT` | `Platform.ios.kt` · `UIDevice.systemName/systemVersion` |

Solo en iOS (sin `expect`, porque Android no las necesita): `MainViewController.kt`
(`ComposeUIViewController { App() }`) y `di/KoinInit.kt` (`initKoinIos()`), que
Swift invoca como `MainViewControllerKt.MainViewController()` y
`KoinInitKt.doInitKoinIos()`.

**Criterio.** Baja a la plataforma solo lo que depende del sistema operativo.
Las reglas de negocio (`Producto.requiereReposicion`, validaciones, el texto
que se comparte, la decisión de estados de `ProductoUiState`) se quedan en
`commonMain` aunque se usen dentro de una capacidad nativa. `commonMain` no
importa `android.*` ni `platform.*`. `InfoDispositivo` es una `expect class`
(Beta en Kotlin 2.x), por eso `shared/build.gradle.kts` agrega
`-Xexpect-actual-classes`.

## Estructura del proyecto
- `shared/commonMain`: lógica de negocio y UI compartida (modelos, dominio, presentación)
- `shared/androidMain`: implementación específica de Android (bindings nativos)
- `shared/iosMain`: implementación específica de iOS (bindings nativos)
- `androidApp`: módulo de la aplicación Android
- `iosApp`: proyecto Xcode para iOS

## Requisitos
- JDK 17+
- Android Studio con SDK configurado
- Xcode (solo en macOS, para compilar iOS)

## Notas de entorno
Desarrollado en Windows. La lógica de negocio y la app Android fueron
validadas localmente. Desde Windows se compilan los klibs de iOS
(`:shared:compileKotlinIosSimulatorArm64`), así que el código de `iosMain` se
verifica; enlazar y ejecutar la app iOS requiere un equipo macOS con Xcode.
