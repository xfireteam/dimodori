# Búsqueda por voz en Android TV

## Comportamiento

En Buscar hay dos botones circulares de 48 dp: lupa y micrófono, con iconos
de 22 dp y el mismo estilo de foco relleno. Están disponibles tanto en la
vista inicial como cuando se muestra el teclado propio y los resultados.
Izquierda/derecha permiten moverse entre ambos; abajo vuelve al contenido
o al teclado, según la vista.

El micrófono abre `ACTION_RECOGNIZE_SPEECH` mediante Activity Result. El
proveedor del sistema se encarga del micrófono, sus permisos y su conexión.
DIMODORI no incorpora un servicio de voz ni solicita `RECORD_AUDIO`, no
escucha en segundo plano y no captura el botón de voz físico del mando.
El idioma enviado corresponde a la configuración de idioma de la app.
Un dispositivo sin proveedor utilizable conserva el teclado y recibe un aviso.
No se utiliza `resolveActivity`, que podría dar falsos negativos por las reglas
de visibilidad de paquetes; se captura el fallo real al abrir la actividad.

Una respuesta válida sustituye la consulta y pasa por la búsqueda y los
filtros actuales. Cancelar, recibir texto vacío o fallar no modifica la
consulta anterior. Se devuelve el foco al micrófono al reanudarse la
pantalla. Las pulsaciones completas se procesan con el mismo control de
activación que los selectores TV: se abre después de soltar OK/Enter,
no en KeyDown, y se consumen las repeticiones.

Cada dictado pertenece a su pantalla y a su servidor/cuenta. Cambiar de
ámbito o abandonar la pantalla invalida su respuesta. No se conserva
una solicitud pendiente al recrear la composición: una respuesta antigua
se descarta, en vez de aplicarse a una pantalla nueva.

## Verificación local

Con `kotlinc`, Java y Python en PATH:

```sh
python3 scripts/check-tv-voice-search.py
```

Ejecuta las clases de producción de control de solicitud y pulsación, y
comprueba el enlace de controles, recursos y permisos. No es una prueba
de Android, del motor de voz, del servidor ni de representación visual.

Con SDK Android y un dispositivo/emulador:

```sh
./gradlew :phone:compileDebugKotlin :phone:compileDebugAndroidTestKotlin
./gradlew :phone:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.package=com.dimodori.app.tv.ui.screens.dashboard.search
```

Las pruebas instrumentadas cubren dimensiones, navegación entre botones y
pulsaciones físicas completas/repetidas/canceladas de los controles reales.
También se prueba el adaptador real de Activity Result con un proveedor simulado:
respuesta válida, cancelación, vacío, error, proveedor inexistente, solicitudes
duplicadas, cambio de servidor y abandono/recreación de pantalla. El proveedor
simulado no confirma que un mando o servicio de voz concreto funcione.

## Comprobación en televisor real

1. Con un mando con micrófono y un proveedor de voz disponible, abrir Buscar
   y verificar lupa/micrófono contiguos e iguales, también con teclado abierto.
2. Elegir micrófono con OK, dictar un título en español y comprobar consulta,
   resultados del servidor y filtros. Editar y borrar con el teclado.
3. Repetir con una consulta anterior: cancelar con Atrás, guardar silencio
   y provocar un error del proveedor. La consulta permanece, el foco vuelve
   al micrófono y no se cierra la búsqueda.
4. Mantener OK pulsado: una sola ventana de dictado, abierta al soltar.
   Confirmar que no vuelve a abrirse automáticamente al regresar.
5. Repetir sin proveedor de voz o con acceso al micrófono denegado al
   proveedor. El aviso permite continuar por teclado; no hay cierre de app.
6. Cambiar de servidor/cuenta o abandonar Buscar mientras el resultado esté
   pendiente; no aplicar ese resultado a otro ámbito. Comprobar también
   recreación de actividad con resultado pendiente.

Sin SDK ni dispositivo no se pueden confirmar compilación Android, apariencia
en TV ni reconocimiento con un mando real. No usar la vista web de DIMODORI
Control como sustituto de estas comprobaciones nativas.
