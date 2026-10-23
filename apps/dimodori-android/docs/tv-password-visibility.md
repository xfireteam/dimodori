# Mostrar la contraseña con el mando en TV

El ojo es ahora un botón hermano del campo, no un control anidado en el
editor. Conserva la posición derecha del campo y tiene una superficie de
48 dp. Al enfocarlo se rellena de blanco con icono negro, sin añadir el
borde blanco reservado a tarjetas de contenido.

## Recorrido

- Escribir la contraseña con el teclado de Android TV como antes.
- Cerrar el teclado con Atrás. Derecha o Abajo desde el campo llevan al ojo
  después de soltar la tecla; las repeticiones no saltan al siguiente control.
- OK/centro, Enter o Enter numérico alternan una vez después de soltar.
- Izquierda o Arriba regresan al campo. Abajo avanza a Iniciar sesión o,
  si no hay usuario y está disponible, a Quick Connect.
- Con el teclado abierto se conserva su navegación y edición. No se captura
  Atrás ni se invoca el acceso desde el ojo.

El campo mantiene su posición en la composición y el adaptador de texto
de Compose conserva la selección al cambiar únicamente la transformación
visual. No se modifica el texto al pulsar el ojo, ni se guardan o registran
contraseñas nuevas. El tipo de teclado permanece Password incluso cuando
el texto se muestra. La visibilidad comienza desactivada y el ojo queda
deshabilitado durante acceso normal o Quick Connect en curso.

Sólo cambia el formulario reutilizado de TV; móvil, ViewModels y servidores
no se modifican.

## Comprobaciones

```sh
python3 scripts/test-tv-password-contract.py
kotlinc phone/src/main/java/com/dimodori/app/tv/ui/screens/detail/TrackSelectorActivation.kt \
  scripts/TrackSelectorActivationChecks.kt -include-runtime -d /tmp/tv-press-checks.jar
java -jar /tmp/tv-press-checks.jar
```

Estas comprobaciones validan el enlace del código y la clase real de
pulsaciones, no el foco ni el teclado nativos.

Con SDK Android y dispositivo/emulador TV:

```sh
./gradlew :phone:compileDebugKotlin :phone:compileDebugAndroidTestKotlin
./gradlew :phone:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.dimodori.app.tv.ui.screens.auth.TvPasswordFieldTest
```

Las pruebas usan el LoginForm real con texto ficticio y cubren retorno al
campo/cursor, alternancia, repeticiones, cancelación, estado ocupado y acceso
al siguiente botón. No requieren credenciales ni conexión a un servidor.
Validar además en el televisor del usuario el cierre del IME con Atrás,
el foco visible y la entrada inicial/reautenticación; los proveedores de
teclado de TV pueden tener diferencias de navegación.
