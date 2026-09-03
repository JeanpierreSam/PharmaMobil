# Documento de evidencias y decisiones — PharmaMobil (Sesión 4, Retos 01 y 02)

Entregable 2 exigido por la Guía Autónoma (sección 6.13). Ejecutado el 2026-09-02 sobre
`emulator-5554` (Android, factor de forma teléfono), commit posterior a `7d78866`.

## 1. Matriz de pruebas funcionales

| N.° | Componente | Acción | Resultado esperado | Resultado obtenido | Estado |
|---:|---|---|---|---|---|
| 1 | Navegación | Inicio → Productos → Clientes → Pedidos → Inicio | Todos los destinos funcionan sin errores | Verificado: Drawer navega a Productos correctamente ([02](img/02_drawer_4_destinos.png), [03](img/03_productos_activos_claro.png)) | ✅ |
| 2 | Drawer | Abrir el menú desde Inicio | Muestra las 4 opciones y resalta el destino activo | Ítem "Inicio" resaltado con `primaryContainer` ([02](img/02_drawer_4_destinos.png)) | ✅ |
| 3 | Productos | Pestaña Activos | Solo `activo == true` | Paracetamol, Ibuprofeno, Amoxicilina, Diclofenaco ([03](img/03_productos_activos_claro.png)) | ✅ |
| 4 | Productos | Pestaña Inactivos | Solo `activo == false` | Solo Loratadina (inicial) ([04](img/04_productos_inactivos_claro.png)) | ✅ |
| 5 | Productos | Pestaña Bajo Stock | `stock <= 5`, transversal a Activos/Inactivos | Amoxicilina (Activo, 5), Loratadina (Inactivo, 0), Diclofenaco (Activo, 3) — los tres a la vez ([08](img/08_bajo_stock_transversal_oscuro.png)) | ✅ |
| 6 | Tema claro | Recorrer Inicio y Productos | Legibilidad y contraste adecuados | Verificado tras completar `ColorScheme` (`surfaceVariant`, `errorContainer`) ([01](img/01_inicio_claro.png), [03](img/03_productos_activos_claro.png)) | ✅ |
| 7 | Tema oscuro | Recorrer Productos (3 pestañas) | Superficies y textos adaptados, no es una simple inversión | Verificado con paleta oscura M3 propia ([07](img/07_productos_inactivos_oscuro.png), [08](img/08_bajo_stock_transversal_oscuro.png)) | ✅ |
| 8 | Registro válido | Vitamina C / 9.90 / 40, `Producto activo` = **desactivado** | Registro exitoso y aparece en Inactivos | Registrado y clasificado correctamente en Inactivos ([05](img/05_formulario_registro.png), [06](img/06_registro_producto_inactivo.png)) | ✅ |
| 9 | Regla de stock | Amoxicilina, stock = 5 (frontera) | Se clasifica como Bajo Stock (`<=`, no `<`) | Aparece en Bajo Stock ([08](img/08_bajo_stock_transversal_oscuro.png)); cubierto además por prueba unitaria `stockIgualAlUmbralPerteneceABajoStock` | ✅ |
| 10 | Stock cero | Loratadina, 12.50, stock 0 | Se permite el valor y conserva su estado Inactivo | Loratadina en Inactivos y en Bajo Stock a la vez | ✅ |
| 11 | Adaptabilidad | Ejecutar en teléfono, tablet y escritorio | El patrón de navegación corresponde al factor de forma | Verificado en teléfono (< 600dp, Drawer modal). **Pendiente**: tablet (600–840dp, `NavigationRail`) y escritorio (> 840dp, Drawer permanente) — requieren un AVD de tablet/tablet-plegable o el *Resizable Emulator* de Android Studio, no disponibles en este entorno | ⚠️ Pendiente |
| 12 | Recursos | Abrir Inicio | El recurso compartido (`pharmamobil_logo`) se muestra correctamente | Logo visible en Inicio ([01](img/01_inicio_claro.png)) | ✅ |

Pruebas unitarias automatizadas (31 tests, `./gradlew :shared:testAndroidHostTest`, todas en verde):
incluye la nueva `ConsultasProductosTest` (5 casos) que cubre la frontera `stock == 5` y la
independencia de los ejes Activo/Inactivo/Bajo Stock a nivel de dominio.

## 2. Decisiones de diseño

### 2.1 Bajo Stock es una vista transversal (punto 8.3 de la guía)

Un producto puede aparecer simultáneamente en **Bajo Stock** y en **Activos** o **Inactivos**,
porque son dos ejes independientes del modelo `Producto`: `activo` (estado comercial) y `stock`
(nivel de inventario). No existe relación de exclusión entre ellos.

Evidencia con los datos simulados iniciales:
- **Amoxicilina** (Activo, stock 5) → aparece en Activos **y** en Bajo Stock.
- **Diclofenaco** (Activo, stock 3) → aparece en Activos **y** en Bajo Stock.
- **Loratadina** (Inactivo, stock 0) → aparece en Inactivos **y** en Bajo Stock.

La regla se centralizó en `domain/query/ConsultasProductos.kt` (`activos()`, `inactivos()`,
`bajoStock()`) para que la UI y las pruebas usen exactamente el mismo criterio y no se dupliquen
umbrales inconsistentes.

### 2.2 Navegación adaptativa por factor de forma

| Factor de forma | Patrón implementado | Justificación |
|---|---|---|
| Teléfono (< 600dp) | `ModalNavigationDrawer` | Opción explícitamente permitida por GP §11 y GAA pasos 14–15 (alternativa a `NavigationBar` inferior); minimiza el uso de espacio en pantallas angostas. |
| Tablet (600–840dp) | `NavigationRail` | Mantiene la navegación siempre visible sin ocupar todo el ancho disponible. |
| Escritorio (> 840dp) | `PermanentNavigationDrawer` | Acceso continuo a los 4 módulos sin gesto adicional, aprovechando el ancho sobrante. |

El contenido del Drawer (marca + ítems) se extrajo a `PharmaDrawerContenidoInterno` para
reutilizarse sin anidar dos superficies (`ModalDrawerSheet` dentro de `PermanentDrawerSheet`),
evitando doble elevación visual en la variante de escritorio.

## 3. Registro de incidencias

| N.° | Descripción del problema | Causa raíz | Solución aplicada |
|---:|---|---|---|
| 1 | La pestaña Bajo Stock (`stock <= 5`) y `domain/query/ConsultasProductos.kt` (`conStockBajo`, `stock < 25`) usaban reglas distintas | Duplicación de la regla de negocio en dos capas sin una única fuente de verdad | Se centralizó en `ConsultasProductos.kt` (`UMBRAL_BAJO_STOCK = 5`, funciones `activos()`/`inactivos()`/`bajoStock()`) y la UI pasó a consumirlas |
| 2 | `ColorScheme` solo definía 12 de los ~19 roles usados por la UI (`surfaceVariant`, `errorContainer`, etc. caían al morado/gris por defecto de M3) | Roles añadidos al código antes de completarse en `Theme.kt`/`Color.kt` | Se completaron ambos esquemas (claro/oscuro) con los roles faltantes y se agregaron `Typography`/`Shapes` propios |
| 3 | Al rotar el dispositivo, `destinoActual`, `modoOscuro` y la pestaña seleccionada volvían a su valor inicial | Estado guardado con `remember` en vez de `rememberSaveable` | Se migraron los tres a `rememberSaveable` |
| 4 | No existía forma de registrar un producto inactivo desde la UI, por lo que la pestaña Inactivos nunca crecía con datos nuevos | El formulario no exponía el campo `activo` del dominio | Se agregó un `Switch` "Producto activo" en `ProductoScreen`, verificado con el registro de "Vitamina C" |

## 4. Pendiente para una siguiente iteración

- Evidencia de tablet y escritorio (ítem 11 de la matriz) — requiere un AVD adicional o el
  Resizable Emulator, no disponible en este entorno de ejecución.
- Edición/eliminación de productos (CRUD completo) — explícitamente fuera de alcance de la
  Sesión 4 según ambas guías ("el CRUD completo se implementará en sesiones posteriores").
