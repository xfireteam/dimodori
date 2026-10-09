# Selectores de pistas de Android TV

## Alcance y diagnóstico

La foto del usuario muestra Audio y Subtítulos en la ficha previa a reproducir,
no los diálogos del reproductor. Ambos, y Vídeo/versiones, comparten el mismo
selector. El código anterior mezclaba el ancla y la expansión automática de
`ExposedDropdownMenuBox` con una apertura manual en `KeyDown` que dejaba pasar
`KeyUp`, sin solicitar explícitamente el foco de la opción actual.

Se aisló esa secuencia en código, pero **no se confirmó en un televisor qué
callback producía el cierre observado**. Transferir el foco antes de terminar
la pulsación también permite que sus repeticiones lleguen a la ventana nueva.
La reparación elimina esos mecanismos competidores, sin sustituir la ficha.

## Cambios

- Menú anclado `DropdownMenu` con ventana enfocable, manteniendo el botón,
  textos, colores, borde y contenido existentes.
- Un único manejo de teclas para OK/centro, Enter y Enter numérico. Consume
  ambas fases y activa una sola vez al soltar una pulsación completa; ignora
  repeticiones sueltas, liberaciones sin pulsación previa y cancelaciones.
- Las opciones usan el mismo manejo para no confirmar por una repetición
  heredada. Perder el foco reinicia la pulsación, pero no cierra el menú.
- Al abrir, se solicita foco en la opción actual, o en la primera válida.
  La lista conserva navegación y desplazamiento, con altura limitada.
- Atrás y la selección cierran el menú y solicitan foco en el botón original.
  El popup recibe Atrás antes de la ficha. La selección cierra primero, antes
  de que un callback de vídeo/versiones pueda navegar.
- Se mantienen clics de puntero y acciones de accesibilidad.
- No se modificaron persistencia, preferencias, índices de pistas, motores,
  interfaz móvil, PiP, dependencias, firma ni publicación.

Se mantiene **1.1.6 / código interno 31**. El único archivo existente de
producción modificado es el selector de la ficha de TV; los otros 325 archivos
Android de fuente/configuración registrados conservaron sus SHA-256 previos.
Las funciones originales de opciones e índices permanecen idénticas.

## Comprobaciones realizadas

- 24 comprobaciones JVM sobre el manejo de pulsaciones **de producción**:
  tres teclas, pulsación mantenida, repetición suelta, cancelación, pérdida de
  foco, liberación de otra tecla y siguiente pulsación válida.
- 16 comprobaciones JVM sobre las funciones originales de opciones e índices:
  índices no consecutivos, orden, etiquetas duplicadas, subtítulos externos,
  desactivado, opciones vacías y pista única.
  El comprobador extrae las funciones y el DTO reales; solo omite las
  anotaciones de serialización para compilarlos sin Android ni ese plugin.
  Esto no comprueba deserialización, reproducción ni compilación de la app.
- Pasaron 6 contratos nuevos de conexión del selector, 6 contratos existentes
  de lanzamiento TV y 8 contratos existentes de PiP. Son pruebas de fuente.
- El parser Kotlin comprobó los 7 archivos Kotlin añadidos/modificados sin
  errores de sintaxis; no equivale a resolver tipos Android/Compose.
- `git diff --check` pasó.

Se añadieron 5 pruebas JUnit y 5 pruebas Android instrumentadas. Las
instrumentadas inyectan eventos de plataforma para comprobar pulsación larga,
flechas, selección única, Atrás, foco al regresar, reapertura, repeticiones
heredadas/cancelación, lista larga de subtítulos y callback de vídeo/versiones.
**No se ejecutaron aquí.** Los fixtures no prueban reproducción real.

## Bloqueo de compilación y prueba nativa

Se intentaron compilación Kotlin debug/release, compilación de instrumentación
y pruebas JUnit mediante Gradle. El intento offline no resolvió AGP 9.2.1 en
la caché. El intento online resolvió los plugins y configuró los proyectos,
pero terminó con **SDK location not found**, antes de compilar. No hay `adb`
disponible ni dispositivo/emulador verificado. No se generó APK/AAB.

Pendiente: compilar con SDK 37, ejecutar las pruebas y verificar en Android TV
la secuencia completa con un mando real, incluyendo teclas mantenidas, listas
largas, Atrás, foco al cerrar, cambio de versión y reproducción con las
preferencias elegidas. No confundir esta comprobación con la entrega de
paquetes de Play a un dispositivo.

## Hallazgo independiente: pistas sin título

El comprobador de índices reprodujo un problema previo, ajeno al cierre:
con una pista Audio de índice 2 sin `displayTitle`, seguida de «Español» de
índice 8, las opciones omiten la primera pista pero la búsqueda del índice
usa la lista completa. Elegir «Español» devuelve **2 en vez de 8**.
Las etiquetas inversas y los subtítulos pueden sufrir el mismo desfase.

No se cambió esa lógica dentro de esta reparación del menú. Requiere una
corrección separada que mantenga cada etiqueta vinculada a su pista real,
además de pruebas de etiquetas ausentes y duplicadas en ambos sentidos.

## Repetir comprobaciones sin SDK

Desde el directorio de la app:

```sh
kotlinc \
  phone/src/main/java/com/dimodori/app/tv/ui/screens/detail/TrackSelectorActivation.kt \
  scripts/TrackSelectorActivationChecks.kt -include-runtime -d /tmp/tv-activation.jar
java -jar /tmp/tv-activation.jar
python3 scripts/check-tv-track-options.py
python3 scripts/test-tv-track-selector-contract.py
python3 scripts/test-tv-launch-contract.py
python3 scripts/test-pip-contract.py
```
