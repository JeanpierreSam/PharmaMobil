# Checklist de entrega · PharmaMobil 1.0.0

Cada punto se marca cuando alguien lo comprobó. Evidencia = captura numerada o comando. Los puntos sin ✔ siguen pendientes.

| Bloque | Punto | Responsable | Fecha | Evidencia | ✔ |
|---|---|---|---|---|---|
| Repositorios | `feature/builds-14` y `feature/entrega-14` fusionadas en `develop` | Jeanpierre | 13-11-2026 | `git log --graph` (B-02) | |
| Repositorios | `v1.0-final` sobre el commit compilado, en PharmaMobil | Jeanpierre | 13-11-2026 | `git show v1.0-final` (B-03) | |
| Repositorios | Backend: sin etiqueta propia; es un clon sin cambios del repositorio del curso, commit `fff44f9` | Jeanpierre | 13-11-2026 | Ficha D | ✔ |
| Repositorios | Commits propios de cada integrante | Jeanpierre | 13-11-2026 | `git shortlog -sn` (B-04) | |
| Secretos | Sin `.jks`, `keystore.properties`, `google-services.json` en el historial | Jeanpierre | 13-11-2026 | B-01 (`git log --all -- …` sin resultados) | ✔ |
| Secretos | Sin contraseñas de firma ni tokens en el código (solo la lectura de `keystore.properties` en `androidApp/build.gradle.kts`) | Jeanpierre | 13-11-2026 | B-01 (`git grep`) | ✔ |
| Secretos | Las credenciales de la guía A son las de prueba públicas del backend del curso | Jeanpierre | 13-11-2026 | Producto A §3 | ✔ |
| Builds | `.aab` y `.apk` existen, firmados, con nomenclatura `Equipo12_ProyectoFinal_Android.*` | Jeanpierre | 11-11-2026 | `apksigner` y `keytool` (`docs/builds-14.md`) | ✔ |
| Builds | La huella de la ficha D coincide con `apksigner` del archivo entregado | Jeanpierre | 11-11-2026 | `docs/builds-14.md` | ✔ |
| Builds | `versionName` 1.0.0 / `versionCode` 1 coinciden con el archivo | Jeanpierre | 13-11-2026 | `aapt dump badging` o *Analyze APK* (B-05) | |
| Pruebas | Matriz con 3 dispositivos reales (e iOS con limitación documentada) | Jeanpierre | 11-11-2026 | Producto C | ✔ |
| Pruebas | Recorrido de 8 pasos completo en cada dispositivo | Jeanpierre | 11-11-2026 | Producto C: pasos 5, 6 y 8 en los tres; 3 y 4 sin evidencia | |
| Pruebas | Cada defecto con estado | Jeanpierre | 11-11-2026 | Producto C: ninguno observado | ✔ |
| Pruebas | `:shared:testAndroidHostTest` en verde sobre `v1.0-final` | Jeanpierre | 13-11-2026 | B-06 | |
| Documentación | Informe según la plantilla de la sesión 13 | <nombre> | <fecha> | — | |
| Documentación | Guía de instalación probada por un tercero (registro incluido) | <nombre> | <fecha> | Producto A §8 | |
| Documentación | Limitaciones declaradas (H6, H13, iOS, ícono) | Jeanpierre | 11-11-2026 | `docs/builds-14.md` | ✔ |
| Sustentación | Cada integrante sabe qué módulo defiende: <nombre → módulo> | <nombre> | <fecha> | — | |
| Sustentación | Plan si falla la red: zona con cobertura del PC (192.168.137.1) + APK ya instalado en teléfonos + video de respaldo del recorrido | <nombre> | <fecha> | — | |
| Sustentación | Backend arranca en menos de 2 min (`docker start oracle-local` y `mvnw spring-boot:run` probados esa mañana) | <nombre> | <fecha> | — | |

## Lo que se encontró abierto y cómo se resolvió

| Fecha | Hallazgo | Resolución |
|---|---|---|
| 11-11-2026 | La zona con cobertura recibió `192.168.137.2` porque el adaptador de VMware VMnet1 ocupaba la `.1` | Se deshabilitó VMnet1 y se reinició la zona; la app apunta a `192.168.137.1` |
| 11-11-2026 | La guía preveía que R8 fallaría con clases ausentes de Ktor y Tink | Compiló sin reglas y sin `missing_rules.txt`; solo se conservaron los atributos para Crashlytics |
| 11-11-2026 | `keytool -printcert -jarfile` falla con la configuración regional en español | Se ejecutó con `JAVA_TOOL_OPTIONS=-Duser.language=en` |
| 11-11-2026 | Las capturas «Acerca del teléfono» muestran IMEI, serie y teléfono | Se recortan antes de incluirlas en el PDF |
