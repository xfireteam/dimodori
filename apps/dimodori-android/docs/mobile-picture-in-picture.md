# Ventana flotante en Android móvil

## Comportamiento

PiP nativo mantiene la misma instancia de ExoPlayer o MPV y el mismo
ViewModel del reproductor principal. No cambia identidad, versión, motor
elegido, firma ni distribución. TV conserva su entrada y su interfaz.

- Inicio/cambio de aplicación durante un video activa PiP si el dispositivo
  lo soporta y el usuario lo permite. Android 12+ usa entrada automática;
  Android 8.1–11 usa el aviso de salida de la actividad.
- El botón **Reproductor flotante** permite entrar manualmente, incluso con
  el video pausado. Una pausa manual se conserva al entrar o expandir.
- Mover, redimensionar, expandir y cerrar utiliza la interfaz de Android.
  Los gestos y tamaños dependen de versión/fabricante; desde Android 12 hay
  doble toque y gesto de pinza. No se solicita permiso de superposición.
- Dentro de PiP solo se compone video/subtítulos; barras, menús, controles
  grandes y diálogos se ocultan sin quitar la superficie de video.
- La acción nativa reproducir/pausar se sincroniza con el motor. El receptor
  no es exportado; cada intención inmutable se limita a la app y a la
  sesión vigente, evitando acciones antiguas sobre otro video.
- Expandir vuelve al mismo reproductor y posición sin reinicializarlo.
  Las superficies existentes se redimensionan y MPV se vuelve a conectar
  si Android recrea su superficie; no se recarga el medio por ello.
- Cerrar PiP, o dejar de ser visible mientras estaba en PiP, detiene y
  libera la reproducción. Atrás en pantalla completa sigue saliendo
  explícitamente del reproductor.
- No se activa automáticamente para catálogo/login/ajustes, trailers
  promocionales, audio sin video, TV ni una reproducción Cast sin motor
  local. PiP deshabilitado/no soportado conserva la pausa de segundo plano.
- No se añade reproducción de audio con pantalla apagada ni garantía de
  continuidad después de matar el proceso o forzar la detención de la app.

## Consideraciones de ciclo de vida

Una ventana PiP visible deja la actividad en PAUSED: no debe pausarse el motor
solo por ON_PAUSE cuando se entra o se permanece en PiP. Cuando pasa a STOPPED,
ya no es visible; se cierra la sesión PiP o se pausa el reproductor ordinario.
La expansión visible se distingue del cierre. La referencia de sesión se
limpia antes de ejecutar el cierre para evitar liberaciones repetidas.

La carga normal pendiente se cancela al liberar el reproductor. Una pausa
durante peticiones de red se conserva antes de crear el motor, evitando que
una respuesta tardía inicie audio oculto. También se conserva ese estado en
la preparación de trailers, aunque no estén habilitados para PiP.

El reporte final toma la posición antes de liberar el motor y puede terminar
independientemente del ViewModel, con un límite de 15 segundos y errores
registrados. Así no se reporta posición cero después de liberar el motor ni
se cancela el reporte únicamente porque el usuario cerró PiP. Las descargas
mantienen su almacenamiento de progreso existente.

## Verificaciones ejecutadas aquí

- 28 comprobaciones **ejecutables de la política Kotlin de producción**:
  combinaciones de reproducción/capacidad/permiso, pausa/transición y
  proporciones válidas incluyendo su conversión a Rational.
- Comprobaciones de contrato de fuente para el manifiesto, ambos motores,
  controles/recursos, actividad, receptor, cierre y carga pendiente.
- Las seis comprobaciones existentes de entrada móvil/TV.
- Análisis de sintaxis con el parser real de Kotlin de los archivos nuevos
  y modificados; no equivale a resolución de tipos Android.
- `git diff --check`.

Comandos desde la raíz del repositorio:

```sh
mkdir -p /tmp/dimodori-pip-checks
kotlinc \
  apps/dimodori-android/phone/src/main/java/com/dimodori/app/ui/playerpip/PipPlaybackPolicy.kt \
  apps/dimodori-android/scripts/PipPolicyChecks.kt \
  -include-runtime -d /tmp/dimodori-pip-checks/policy.jar
java -jar /tmp/dimodori-pip-checks/policy.jar
python3 apps/dimodori-android/scripts/test-pip-contract.py
python3 apps/dimodori-android/scripts/test-tv-launch-contract.py
```

## Verificación nativa pendiente

Se intentó:

```sh
cd apps/dimodori-android
./gradlew --no-daemon \
  :phone:testDebugUnitTest :phone:assembleDebug :phone:compileDebugAndroidTestKotlin
```

Gradle se detuvo antes de compilar: **SDK location not found**. Este entorno
no tiene SDK, adb, emulador ni dispositivo conectado. No se generó APK/AAB ni
se publicó. No se ha comprobado todavía continuidad visual/sonora en hardware.

Se incluyen tres pruebas JUnit de política y cuatro pruebas instrumentadas
de contratos del controlador sobre una actividad de prueba exclusiva de
debug. Las instrumentadas simulan callbacks para verificar pausa, expansión,
cierre único y disposición de pantallas antiguas: **no son pruebas de video
real ni equivalen a que el sistema haya mostrado una ventana PiP**.

Con Android SDK y emulador/dispositivo disponible:

```sh
./gradlew :phone:testDebugUnitTest :phone:connectedDebugAndroidTest
```

### Matriz obligatoria antes de distribuir

Usar videos/cuentas/dispositivos de prueba, sin borrar datos de uso real.
Repetir con **ExoPlayer y MPV**, reproducción de servidor y descarga local:

| Caso | Resultado esperado |
| --- | --- |
| Android 8.1–11: Inicio y cambio de app | Entra en PiP sin detener el video |
| Android 12+: Inicio por gesto y botones, cambio de app | Entrada automática suave y sin pausa |
| Android 15+: transición | No aparecen barras/diálogos grandes en la animación |
| Acción manual con video reproduciendo o pausado | Entra con el estado anterior, sin reproducir algo pausado |
| Pausa previa + Inicio | No entra automáticamente ni deja audio oculto |
| Expandir después de varios segundos | Mismo motor, video, posición, pistas y sesión |
| Mover, reducir y agrandar | Video/subtítulos visibles; sin recargar el medio o perder superficie |
| Acción PiP reproducir/pausar | Estado e icono sincronizados y sin audio doble |
| Cerrar PiP y volver a DIMODORI | Sin reproducción oculta; fuera del reproductor; progreso conservado |
| Atrás en pantalla completa | Sale y libera; no fuerza PiP |
| PiP denegado en ajustes / dispositivo sin soporte | Mensaje manual o botón oculto; pausa al salir; sin fallo |
| Salir/cerrar durante una carga lenta o un cambio de episodio | No aparece un motor tardío con audio oculto |
| Bloquear pantalla con PiP | No continúa audio cuando la actividad deja de ser visible |
| Catálogo, login, ajustes, trailer promocional, Cast remoto | No se activa PiP |
| Android TV | Mismo arranque/rutas previas; no se añade PiP a su actividad |

Confirmar además orientación al expandir, subtítulos con diversas proporciones,
HDR, progreso final en el servidor y ausencia de sesiones/reportes duplicados.
Registrar versión de Android, modelo, motor, origen del video y resultados;
no sustituir esta matriz por las comprobaciones de fuente o del parser.

Referencias:
- https://developer.android.com/develop/ui/views/picture-in-picture
- https://developer.android.com/develop/ui/compose/system/picture-in-picture
