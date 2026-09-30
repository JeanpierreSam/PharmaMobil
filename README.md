# PharmaMobil

Aplicación multiplataforma para la gestión integral de clientes,
productos, pedidos e inventario farmacéutico.

## Stack Tecnológico
- Kotlin Multiplatform (KMP)
- Compose Multiplatform
- Material 3 (theming corporativo, modo claro/oscuro)

## Estado actual (Sesión 7 — Cliente Ktor)
- Módulo de Productos en capas Clean Architecture + MVVM con Koin (Sesión 5).
- La lista de productos se obtiene de una API REST con Ktor Client (Sesión 7);
  ver [Conectividad REST](#conectividad-rest).
- Navegación principal con `ModalNavigationDrawer` + `Scaffold` + `TopAppBar`,
  con 4 destinos tipados (Inicio, Productos, Clientes y Pedidos) y navegación
  adaptativa por factor de forma (Sesión 4).
- Identidad visual centralizada en `PharmaMobilTheme` con modo claro/oscuro.

El registro de productos contra la API y el CRUD completo quedan para la
Sesión 8, junto con el backend PharmaSoft.

## Conectividad REST

**API de práctica:** Platzi Fake Store API, versión v1.
URL base: `https://api.escuelajs.co/api/v1/`, definida en una sola constante
(`data/remote/ConfiguracionApi.kt`) para cambiarla en una línea en la Sesión 8.

**Endpoint consumido:** `GET /products?limit=10&offset=0`, que devuelve un
arreglo de productos.

**DTO:** `ProductoDto` (`id`, `title`, `price`, `description`, `images`,
`category`) y `CategoriaDto` (`id`, `name`), en `data/remote/dto`. El mapper
`toDomain()` los convierte en `Producto`; como la API no expone inventario ni
estado, usa `stock = 10` y `activo = true` como valores temporales. Los
productos que no cumplen las reglas del dominio (nombre vacío, precio 0) se
descartan sin afectar al resto de la lista.

**Cliente e inyección:**
- `crearHttpClient(engine)` configura `ContentNegotiation` (JSON con
  `ignoreUnknownKeys`), `Logging` (cabeceras, con prefijo `Ktor:` en
  Logcat/Xcode), `HttpTimeout` (15 s petición, 10 s conexión) y
  `DefaultRequest` (URL base y `Content-Type`).
- El motor lo aporta cada plataforma en `platformModule`: OkHttp en Android y
  Darwin en iOS.
- `dataModule` crea un único `HttpClient`, `ProductoApi` y
  `ProductoRepositorioRest`, que implementa `ProductoRepository` sin cambiar
  su interfaz. `ProductoRepositorioEnMemoria` se conserva para pruebas.

**Errores:** `ejecutarLlamada` (`data/remote`) es el único punto que traduce
las excepciones de Ktor a `ErrorApi` (`domain/error`). La capa de presentación
solo conoce `ErrorApi` y nunca importa `io.ktor`.

| `ErrorApi` | Origen | Mensaje al usuario |
|---|---|---|
| `NoEncontrado` | HTTP 404 | No se encontró el recurso solicitado. |
| `Servidor` | HTTP 5xx y otros 4xx | El servidor no está disponible. Inténtalo más tarde. |
| `SinConexion` | `IOException` (sin red) | Sin conexión a internet. Revisa tu red. |
| `TiempoAgotado` | `HttpRequestTimeoutException` | El servidor tardó demasiado en responder. |

**Registro:** `ProductoRepositorioRest.registrar()` lanza
`UnsupportedOperationException` hasta la Sesión 8, en la que se implementa el
`POST` contra PharmaSoft.

**Versión de Ktor:** 3.6.0. Incluye OkHttp 5.5.0, que exige `compileSdk` 37,
por eso el proyecto compila contra la API 37 (`targetSdk` sigue en 36). AGP
9.0.1 muestra un aviso porque está probado hasta la 36.1; el build, las pruebas
y la app en el emulador funcionan.

### Catálogo de endpoints (API de práctica v1)

| Método | Ruta | Parámetros | Respuesta esperada | Errores | Uso en la app |
|---|---|---|---|---|---|
| GET | `/products` | `limit`, `offset` (query) | 200 · arreglo de productos | 5xx | `ProductoApi.obtenerProductos` |
| GET | `/products/{id}` | `id` (ruta) | 200 · un producto | 400 si el id no existe (la API no responde 404) | `ProductoApi.obtenerProducto` |
| POST | `/products` | cuerpo JSON | 201 · producto creado | 400 | Sesión 8 |
| PUT | `/products/{id}` | `id` + cuerpo JSON | 200 · producto actualizado | 400 | Sesión 8 |
| DELETE | `/products/{id}` | `id` (ruta) | 200 · confirmación | 400 | Sesión 8 |

Con el traductor actual, el 400 de un id inexistente llega como
`ErrorApi.Servidor`; la Sesión 8 agrega `Validacion` para los 400.

### Diccionario de DTO

| Campo JSON | Tipo Kotlin | Obligatorio | Por defecto | Campo en el dominio |
|---|---|---|---|---|
| `id` | `Int` | Sí | — | `Producto.id` (`Long`, con `toLong()`) |
| `title` | `String` | Sí | — | `Producto.nombre` |
| `price` | `Double` | Sí | — | `Producto.precio` |
| `description` | `String` | No | `""` | No se mapea |
| `images` | `List<String>` | No | `emptyList()` | No se mapea |
| `category` (`categoria`) | `CategoriaDto?` | No | `null` | No se mapea |
| `category.id` / `category.name` | `Int` / `String` | Sí | — | — |
| — (no existe en la API) | — | — | `stock = 10`, `activo = true` | `Producto.stock`, `Producto.activo` |

La API también envía `slug`, `creationAt`, `updatedAt` y, en `category`,
`slug`, `image`, `creationAt` y `updatedAt`. El DTO no los declara y
`ignoreUnknownKeys = true` hace que se ignoren.

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
