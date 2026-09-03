# PharmaMobil

Aplicación multiplataforma para la gestión integral de clientes,
productos, pedidos e inventario farmacéutico.

## Stack Tecnológico
- Kotlin Multiplatform (KMP)
- Compose Multiplatform
- Material 3 (theming corporativo, modo claro/oscuro)

## Estado actual (Sesión 4 — Retos 01 y 02)
- Navegación principal con `ModalNavigationDrawer` + `Scaffold` + `TopAppBar`,
  con 4 destinos tipados: Inicio, Productos, Clientes y Pedidos.
- Reutilización de `ProductoScreen` (registro de productos) dentro de Productos,
  con Tabs para clasificar el inventario en Activos, Inactivos y Bajo Stock.
- Navegación adaptativa por factor de forma: Drawer modal (teléfono),
  `NavigationRail` (tablet) y Drawer permanente (escritorio).
- Identidad visual centralizada en `PharmaMobilTheme` (colores, tipografía y formas)
  con soporte para modo claro/oscuro.
- Recurso compartido (`pharmamobil_logo`) consumido desde `InicioScreen`.

Los datos de productos son simulados en memoria (`ProductoRepositorySimulado`);
aún no hay persistencia ni consumo de servicios REST — queda para sesiones
posteriores, junto con el CRUD completo de Clientes y Pedidos.

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
