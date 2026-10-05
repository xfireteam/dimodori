# Inicio correcto en Android TV

## Problema y alcance

En una instalación nueva en NVIDIA SHIELD, el usuario observó la interfaz móvil
al abrir DIMODORI por primera vez y TV al salir y volver a abrir. El manifiesto
ofrece una actividad MAIN/LAUNCHER móvil y otra MAIN/LEANBACK_LAUNCHER de TV.
La actividad móvil no seleccionaba el dispositivo: una apertura genérica podía
mostrar la interfaz táctil en un televisor.

No se capturó el Intent real emitido por Google Play. La ruta genérica explica
una causa posible del síntoma; no prueba qué paquete ofrece Play ni el estado
de aprobación, elegibilidad o búsqueda en la tienda.

## Corrección

- La entrada móvil consulta `PackageManager.FEATURE_LEANBACK` en cada creación,
  incluida una recreación con estado guardado. No usa tamaño de pantalla,
  orientación, mando conectado ni preferencias guardadas para elegir TV.
- En TV conserva el splash del sistema, abre explícitamente
  `DimodoriTvActivity` y termina sin componer móvil, comprobar autenticación
  móvil o solicitar permisos de notificaciones móviles.
- Se copia el Intent: acción, datos, tipo, extras y ClipData se conservan.
  MAIN usa categoría Leanback en lugar de Launcher.
- El Intent reenviado conserva permisos de URI y sustituye las banderas de
  lanzamiento por CLEAR_TOP y SINGLE_TOP. No propaga NEW_TASK, CLEAR_TASK,
  MULTIPLE_TASK ni NEW_DOCUMENT: permanece en la tarea del punto de entrada y
  reutiliza TV sin reiniciar su navegación Compose. La entrada móvil termina,
  por lo que Atrás no puede mostrarla.
- Ambas actividades retienen el último Intent cuando son reutilizadas.
- La entrada Leanback directa, identidad, versión, requisitos opcionales de
  hardware, idioma, preferencias y almacenamiento de sesión no se cambian.
  TV sigue realizando su propia inicialización de idioma y autenticación.

Referencia: https://developer.android.com/training/tv/get-started/hardware

## Verificación disponible en este entorno

Comprobaciones de contrato de código, sin SDK:

```sh
python3 apps/dimodori-android/scripts/test-tv-launch-contract.py
git diff --check
```

Estas comprobaciones revisan el orden de la redirección, salida de la actividad
móvil, nueva intención, selección por capacidades, banderas y las dos entradas
del manifiesto. **No son compilación ni pruebas Android en ejecución.**

Se añadieron cinco pruebas JUnit del detector y cinco pruebas instrumentadas
del Intent para ejecutar con Android:

```sh
cd apps/dimodori-android
./gradlew :phone:testDebugUnitTest :phone:assembleDebug :phone:compileDebugAndroidTestKotlin
./gradlew :phone:connectedDebugAndroidTest
```

El intento de compilación y pruebas locales se detuvo antes de compilar por
`SDK location not found`. No hay SDK, adb, emulador ni SHIELD conectado.
Las pruebas JUnit/instrumentadas y la siguiente matriz **no se han ejecutado
en este entorno**. No se generó ni publicó APK/AAB.

## Matriz de aceptación con dispositivo

Usar una instalación de prueba sin datos valiosos para el caso de instalación
limpia. No borrar datos de una instalación de uso real: se perderían sesión,
ajustes y descargas.

| Dispositivo / estado | Apertura | Resultado esperado |
| --- | --- | --- |
| SHIELD o Android TV, instalación limpia | Abrir en Play | TV desde el primer inicio; nunca móvil ni permiso móvil |
| SHIELD o Android TV, instalación limpia | MAIN/LAUNCHER explícito | TV desde el primer inicio |
| SHIELD o Android TV | Icono Leanback | TV directo, operable con D-pad |
| TV con sesión y navegación activa | HOME y MAIN/LAUNCHER de nuevo | Se reutiliza TV, conserva sesión y navegación |
| TV ya abierta | Abrir desde Play otra vez | No se apilan dos actividades TV ni aparece móvil |
| TV después de una redirección | Atrás hasta salir | Ninguna pantalla móvil debajo |
| TV tras muerte de proceso/recreación | Reabrir entrada genérica o Leanback | TV; idioma y sesión persisten conforme al comportamiento existente |
| Teléfono | MAIN/LAUNCHER, primera apertura y reapertura | Interfaz móvil y permisos existentes |
| Tablet, incluso con mando conectado | MAIN/LAUNCHER | Interfaz móvil |

Para aislar las dos entradas sin depender de Play:

```sh
adb shell am start -W -a android.intent.action.MAIN \
  -c android.intent.category.LAUNCHER \
  -n com.dimodori.app/.ui.activity.DimodoriActivity
adb shell am start -W -a android.intent.action.MAIN \
  -c android.intent.category.LEANBACK_LAUNCHER \
  -n com.dimodori.app/.tv.ui.activity.DimodoriTvActivity
adb shell dumpsys activity activities
```

En TV, confirmar la actividad reanudada `DimodoriTvActivity` y la ausencia de
la entrada móvil en la pila después de la redirección. Para reapertura, anotar
la identidad de la actividad TV y la pantalla antes y después. Registrar
dispositivo, versión de Android, origen del APK/AAB y resultados observados.
La apertura real desde Play solo se puede comprobar con un paquete distribuido
por Play; lanzar el componente manualmente no demuestra ese flujo.
