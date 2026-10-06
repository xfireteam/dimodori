# Incorporación selectiva de JellyCine 1.3.5

Se comparó el ZIP adjunto con los cambios de
https://github.com/sureshfizzy/JellyCine/compare/v1.3.4...v1.3.5.
Los 57 archivos modificados del archivo coincidían con los SHA de blob del
comparador. No se sustituyeron módulos completos.

## Correcciones incorporadas

- **HDR al cerrar el reproductor:** el método de `Window` para headroom HDR
  pertenece a Android 15, no a Android 14. Se accede mediante reflexión y
  únicamente desde API 35, siguiendo la corrección de upstream para evitar
  referencias directas que R8 pueda extraer a métodos sintéticos compartidos.
  Un fallo opcional de headroom se registra y no interrumpe la limpieza.
  Se mantienen los valores 4.0 al entrar y 1.0 al salir, además de la
  restauración del modo de color original. No se modificaron los motores,
  sus superficies ni el guardado de progreso.
- **Atrás:** las ocho rutas de ajustes (reproductor, subtítulos, interfaz,
  conexiones, caché, información, servidor y tiempo de pantalla) usan el mismo
  callback que sus botones de regreso. A diferencia del parche literal, los
  handlers se registran en sus rutas, antes del contenido, y solo interceptan
  cuando la pantalla está `RESUMED`. Así los handlers de contenido tienen
  prioridad y una pantalla retenida o pausada en PiP no intercepta Atrás.
  Descargas conserva su handler propio y sus capas de reproducción/selección.
  Ver todo permite cerrar primero su hoja de ordenación. Las pestañas del
  dashboard vuelven a Inicio, salvo con la cuenta o el selector de usuario
  abiertos; el handler se compone después del NavHost interior para no volver
  a la pestaña anterior por el historial interno.
- **Seerr:** la etiqueta informativa usa una `Surface`, no un `AssistChip`
  desactivado. Se conservan los textos, colores y borde; la etiqueta no tiene
  callback de pulsación que intercepte la tarjeta. No se reincorporó Discord.

## Preservación de DIMODORI

Se mantienen **1.1.5 / código interno 30**, identidad `com.dimodori.app`, la
aplicación conjunta móvil/TV y los identificadores compartidos `com.jellycine`.
No se editaron Gradle, versiones, firma, Codemagic, autenticación, selección
de servidores, control remoto ni implementación de TV. Se verificaron los
SHA-256 previos de 16 archivos de configuración, autenticación, actividad,
motores, superficies, progreso, PiP y TV: todos permanecieron idénticos.

No se incorporaron reseñas TMDB, estudios/redes locales, Force Offline,
traducciones ni cambios cosméticos de navegación. Son funciones independientes
que requerirían adaptar el comportamiento propio de DIMODORI.

## Inventario de dependencias: no se actualizaron

| Familia | DIMODORI actual | JellyCine 1.3.5 | Recomendación |
| --- | --- | --- | --- |
| Media3 | 1.10.0 / 1.10.1 | 1.11.1 | Alinear primero en `core` y `phone`; verificar ExoPlayer, PiP, formatos y decoder FFmpeg antes de aceptar. |
| FFmpeg / libmpv | 1.9.0+1 / 1.0.4 | Sin cambio | No asumir compatibilidad binaria del decoder por coincidir con upstream. |
| OkHttp / Okio | 5.3.2 / 3.9.0 | 5.5.0 / 3.18.2 | Evaluar juntas; revisar resolución de Ktor/Coil, autenticación, streaming y descargas. |
| Coil | compose 3.5.0; gif/network/svg 3.4.0 | Todas 3.6.2 | Alinear en `shared` y `phone`; verificar imágenes, GIF/SVG y liberación de caché en reproducción. |
| Compose BOM | 2026.08.00 | 2026.09.00 | Actualizar producción y androidTest juntos; revisar hojas y navegación táctil/TV. |
| Lifecycle | 2.10.0 | 2.11.0 | Revisar callbacks, ViewModels y PiP con los cambios de navegación. |
| Navigation Compose | 2.9.8 | 2.10.1 | Validar ambos NavHost, retorno a Inicio, diálogos y rutas con argumentos. |
| Browser | 1.8.0 | 1.10.0 | Verificar enlaces y autenticación externa. |
| Hilt Navigation Compose | 1.3.0 | 1.4.0 | Actualizar con el conjunto Lifecycle/Navigation. |
| Kotlin | 2.3.21 | 2.4.20 | Última etapa: multiplaforma, Compose y serialización coordinados. |
| KSP | 2.3.11 | 2.3.12 | Validar generación Hilt/Room junto con Kotlin. |
| NewPipeExtractor | `v0.26.5` | `0.26.5` | Cambio de coordenada, no una nueva versión numérica. Ambos POM públicos devolvieron HTTP 200; no se comprobó igualdad de JAR ni resolución Gradle. No cambiar sin validarlo. |
| AGP / Hilt / Compose multiplataforma | 9.2.1 / 2.60.1 / 1.7.1 | Sin cambio | Conservar y verificar compatibilidad al cambiar Kotlin/KSP. |

Archivos inventariados: `build.gradle`, `core/build.gradle`,
`data/build.gradle`, `shared/build.gradle` y `phone/build.gradle`.
Antes de cada etapa: resolución Gradle y compilación debug/release con SDK 37,
pruebas relevantes, comprobación con minificación R8 y dispositivos reales.
No actualizar todas las familias a la vez.

## Verificación realizada y límites

- 59 comprobaciones JVM ejecutaron las funciones de política **de producción**
  para el límite HDR y las condiciones de Atrás del dashboard.
- Pasaron 8 contratos de estas correcciones, 8 contratos existentes de PiP y
  6 contratos de lanzamiento TV. Son comprobaciones de código fuente, no
  pruebas de comportamiento de Android.
- El parser real de Kotlin analizó los 13 archivos Kotlin añadidos/modificados
  sin errores sintácticos. Esto no resuelve tipos Android ni equivale a compilar.
- `git diff --check` pasó; los 16 archivos protegidos no cambiaron.
- Se añadieron pruebas JUnit de las políticas y 5 pruebas Android instrumentadas
  de headroom, prioridad de handlers, pantalla pausada, historial de pestañas y
  pulsaciones en los cinco estados de Seerr. **No se ejecutaron** aquí.

Se intentaron `:phone:testDebugUnitTest`, `:phone:compileDebugKotlin`,
`:phone:compileReleaseKotlin`, `:phone:compileDebugAndroidTestKotlin` y
`:phone:minifyReleaseWithR8`. El primer intento agotó el límite de tiempo
durante la preparación de Gradle; un segundo intento offline logró configurar
los proyectos y terminó con **SDK location not found**, antes de compilar.
No hay `adb` disponible ni dispositivo/emulador verificado. No se generó,
firmó ni publicó APK/AAB.

Pendiente en un entorno Android: compilar debug/release e instrumentación,
ejecutar los tests, inspeccionar la salida R8 y probar reproducción/cierre
con MPV y ExoPlayer en Android 14 y Android 15+, contenido SDR/HDR, PiP
entrar/restaurar/cerrar y progreso final. Verificar Atrás en todas las rutas,
diálogos y Ver todo, pulsar las etiquetas de Seerr y confirmar el control
remoto de TV. Los fixtures de tests no sustituyen reproducción real.

## Repetir comprobaciones sin SDK

```sh
python3 scripts/test-upstream-fixes-contract.py
python3 scripts/test-pip-contract.py
python3 scripts/test-tv-launch-contract.py
kotlinc \
  phone/src/main/java/com/dimodori/app/ui/screens/player/HdrHeadroomPolicy.kt \
  phone/src/main/java/com/dimodori/app/ui/screens/dashboard/DashboardBackPolicy.kt \
  scripts/UpstreamPolicyChecks.kt -include-runtime -d /tmp/upstream-policies.jar
java -jar /tmp/upstream-policies.jar
```

Estas comprobaciones no eliminan la necesidad de la validación Android pendiente.
