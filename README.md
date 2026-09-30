# PharmaMobil

Aplicación multiplataforma para la gestión integral de clientes,
productos, pedidos e inventario farmacéutico.

## Stack Tecnológico
- Kotlin Multiplatform (KMP)
- Compose Multiplatform
- Material 3 (theming corporativo, modo claro/oscuro)

## Estado actual (Sesión 8 — CRUD REST con PharmaSoft)
- Módulo de Productos en capas Clean Architecture + MVVM con Koin (Sesión 5).
- CRUD completo de productos (listar, obtener, crear, actualizar y dar de baja)
  contra el backend del curso **PharmaSoft**, con Ktor Client; ver
  [Conexión con PharmaSoft](#conexión-con-pharmasoft).
- Navegación principal con `ModalNavigationDrawer` + `Scaffold` + `TopAppBar`,
  con 4 destinos tipados (Inicio, Productos, Clientes y Pedidos) y navegación
  adaptativa por factor de forma (Sesión 4).
- Identidad visual centralizada en `PharmaMobilTheme` con modo claro/oscuro.

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
| Obtener | `GET /productos/{id}` (al pulsar Editar) | 200 |
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
validadas localmente. La compilación de iOS requiere un equipo macOS
con Xcode, por lo que queda documentada para integración posterior.
