# PharmaMobil

Aplicación multiplataforma para la gestión integral de clientes,
productos, pedidos e inventario farmacéutico.

## Stack Tecnológico
- Kotlin Multiplatform (KMP)
- Compose Multiplatform
- Material 3 (theming corporativo, modo claro/oscuro)

## Estado actual (Sesión 11 — Persistencia offline-first)
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
- Login con JWT contra PharmaSoft, token en almacén seguro por plataforma y
  permisos en la interfaz; ver [Autenticación](#autenticación).
- Catálogo guardado en SQLite con SQLDelight: la app lista, registra y abre
  sesión sin red, y sincroniza al reconectar; ver
  [Persistencia offline-first](#persistencia-offline-first).

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

## Autenticación

**Flujo.** `LoginScreen` → `SesionViewModel` → `IniciarSesionUseCase` →
`AuthRepositorioRest` → `POST /api/v1/auth/login`. El token se guarda en
`AlmacenSeguro` y el plugin `Auth` (bearer) de Ktor lo adjunta a cada petición;
nadie escribe la cabecera `Authorization` a mano. La interfaz solo observa
`EstadoSesion` (`Verificando`, `Activa`, `Inactiva(aviso)`).

- **401** (sin token, expirado, alterado o usuario deshabilitado): el
  repositorio de productos avisa al `AuthRepository`, que borra el token, olvida
  la caché del cliente y deja la sesión `Inactiva(aviso)`; la app vuelve al
  login con el mensaje literal del servidor.
- **403** (sin permiso): la sesión se mantiene y se muestra el mensaje del
  servidor.

**Dónde se guarda el token.**

| Plataforma | Almacén |
|---|---|
| Android | `EncryptedSharedPreferences` (AES-256, llave en Android Keystore), archivo `sesion_segura.xml` |
| iOS | Keychain (`kSecClassGenericPassword`, servicio `pe.edu.upeu.pharmamobile`, cuenta `jwt`) |

**Registro.** `Logging` muestra `Authorization: Bearer ***` (cabecera saneada) y
se apaga (`LogLevel.NONE`) fuera de compilaciones de depuración.

**Usuarios de prueba (PharmaSoft).**

| Usuario | Clave | Permisos relevantes |
|---|---|---|
| `admin` | `Admin123` | Todos |
| `vendedor` | (ver PharmaSoft) | Catálogo (leer), clientes, ventas |
| `almacen` | `Almacen123` | Catálogo (leer y escribir); sin clientes ni ventas |

**Respuestas de seguridad de PharmaSoft.**

| Situación | Código | `message` |
|---|---|---|
| Sin cabecera o sin prefijo `Bearer ` | 401 | Debe iniciar sesión para acceder a este recurso |
| Token expirado | 401 | El token ha expirado. Inicie sesión nuevamente |
| Token con la firma alterada | 401 | El token no es válido |
| Usuario deshabilitado | 401 | El usuario está deshabilitado |
| Token válido sin el permiso de la ruta | 403 | No tiene permisos para realizar esta operación |
| Login con clave incorrecta | 401 | Usuario o contraseña incorrectos |

**Permisos en la interfaz.** El menú solo muestra Clientes y Pedidos a quien
tiene `CLIENTES_LEER` / `VENTAS_LEER`; el botón de registrar y las acciones de
editar y dar de baja solo aparecen con `CATALOGO_ESCRIBIR`. Al cerrar sesión, los
ViewModels de la sesión se destruyen (`AlcanceDeSesion` en `App.kt`).

## Validación de sesión

El destino **Diagnóstico de sesión** (y el botón «Lanzar GET /productos sin
sesión» del login) **solo existen en compilaciones de depuración**
(`ES_DEPURACION`). Ejecutan peticiones reales contra PharmaSoft con un segundo
`HttpClient` sin plugin Auth (`DiagnosticoApi`) para provocar casos que la app
normal no genera: token alterado, prefijo `bearer`, petición sin cabecera y la
matriz de permisos del usuario actual (cuerpos `{}`: 403 sin permiso, 400 con
permiso).

| Escenario | Resultado esperado | Resultado observado |
|---|---|---|
| 1. Token válido | 200 | _pendiente de ejecutar_ |
| 2. Token expirado | 401 «El token ha expirado. Inicie sesión nuevamente» | _pendiente_ |
| 3. Token alterado | 401 «El token no es válido» | _pendiente_ |
| 4. Sin cabecera | 401 «Debe iniciar sesión para acceder a este recurso» | _pendiente_ |
| 5. Prefijo `bearer` | 401 «Debe iniciar sesión para acceder a este recurso» | _pendiente_ |
| 6. Usuario deshabilitado | 401 «El usuario está deshabilitado» | _pendiente_ |

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

## Persistencia offline-first

La pantalla de Productos ya no lee de PharmaSoft: lee de una base SQLite local
(`pharma.db`) que es la **fuente de verdad**. La red solo la mantiene al día.

**Esquema** (`shared/src/commonMain/sqldelight/pe/edu/upeu/pharmamobile/db/`):

| Tabla | Para qué | Columnas clave |
|---|---|---|
| `producto` | Copia local del catálogo | `activo` (baja lógica), `pendiente` (1 = PharmaSoft aún no lo tiene) |
| `sincronizacion` | Instante de la última sincronización correcta («Actualizado hace…») | fila única `id = 1` |
| `perfil` | Usuario y permisos de la última sesión verificada (sin el token) | fila única `id = 1` |

**Drivers.** Las consultas son comunes; cada plataforma solo abre el archivo
(`CreadorDeDriver`): `AndroidSqliteDriver` en Android y `NativeSqliteDriver` en
iOS (en Xcode: *Other Linker Flags* = `-lsqlite3`). Las pruebas usan una base en
memoria nueva por prueba, también con expect/actual (`JdbcSqliteDriver` en la
JVM, `inMemoryDriver` en iOS).

**Flujo.** `ProductoRepositorioOffline` implementa `ProductoRepository`:

- `observar()` devuelve el `Flow` de la tabla; el ViewModel se suscribe una vez
  y ninguna operación vuelve a pedir la lista.
- `sincronizar()` **primero sube** lo pendiente y **después baja** la lista del
  servidor. La bajada borra solo las filas con `pendiente = 0`, en una
  transacción: lo creado sin red nunca se pierde y la lista no parpadea.
- `registrar()` guarda primero en local. Sin red el producto queda con un **id
  negativo** (`-1`, `-2`…), un espacio que PharmaSoft nunca usa, y la tarjeta
  muestra «Pendiente de envío». Con red, si el POST responde, la fila `-1` se
  cambia por la del servidor (`confirmarSubida`); si PharmaSoft lo rechaza
  (400/409) se borra y el error sigue apareciendo bajo su campo.
- `ProductoRepositorioRest` se reutiliza como «remoto»: ya traduce Ktor a
  `ErrorApi` y cierra la sesión ante un 401.

**Qué funciona sin red:** listar, ver el detalle, abrir el formulario de
edición, registrar productos nuevos y editar o quitar los que siguen
pendientes. Editar o dar de baja un producto que ya existe en PharmaSoft sigue
necesitando red (se muestra «Sin conexión…» y no se pierde nada).

**Sesión sin red.** Al abrir la app, `/auth/me` no se puede llamar sin red. En
lugar de mandar al login, `AuthRepositorioRest` entra con el `perfil` guardado;
el token sigue en el almacén seguro. Si ya expiró, el primer 401 al reconectar
cierra la sesión y lo pendiente se envía tras el nuevo login. El único paso que
exige red es el primer inicio de sesión.

**Limitación:** `ProductoApi.listar()` pide una página de 20; con más productos
en PharmaSoft, la base local solo guarda la primera página.

**Pruebas** (`ProductoRepositorioOfflineTest`, con `PharmaSoftFalso` sobre
`MockEngine`): sincronización con red, registro sin red, envío al reconectar,
la bajada conserva lo pendiente, ids locales sin colisión y sincronizar dos
veces no duplica. `UltimaSincronizacionTest` y `AntiguedadTest` cubren la
barra «Actualizado hace…» (tabla `sincronizacion`, fila única con el instante
de la última sincronización correcta; si falla, no se toca).

```powershell
.\gradlew.bat :shared:testAndroidHostTest --rerun-tasks
```

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
