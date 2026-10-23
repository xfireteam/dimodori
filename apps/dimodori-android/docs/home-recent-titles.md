# Series y películas recién agregadas en Inicio

## Comportamiento

En Android móvil y Android TV se muestran dos filas generales, inmediatamente
después de «Continuar viendo» y antes de las secciones posteriores:

1. Últimas series agregadas.
2. Últimas películas agregadas.

Las novedades por biblioteca se conservan. Si «Continuar viendo» está vacío o
desactivado, las dos filas mantienen su posición antes de las secciones
posteriores. En móvil, la sección de Networks queda después de las nuevas
filas, no entre ellas.

Cada consulta solicita como máximo 24 títulos del usuario del servidor activo,
sin limitarla a una biblioteca y sin depender de Seerr. Se consulta
`Users/{userId}/Items` con:

- `includeItemTypes=Series` o `Movie`.
- `recursive=true`, sin `parentId`.
- `sortBy=DateCreated`, `sortOrder=Descending`.
- Campos de fecha, metadatos de tarjeta y datos del usuario.

No se usa `/Latest` para estas filas: puede agrupar episodios recientes dentro
de una serie antigua. El orden solicitado es por fecha de incorporación del
título, no por estreno ni por último episodio. Se descartan tipos incorrectos,
IDs/nombres vacíos e IDs repetidos, conservando el orden recibido del servidor.

## Estados y navegación

- Las dos consultas son independientes; usan el gestor de consultas existente,
  claves distintas con la sesión activa y caducidad de 60 segundos.
- Se invalidan al volver al inicio/recuperar red y ante señales de actualización
  de datos del usuario.
- No se añaden a las instantáneas persistentes ni se muestran sin conexión.
  Se conserva la pantalla de descargas/sin conexión existente.
- Una fila vacía se oculta después de terminar la carga; se muestra un esqueleto
  mientras carga o un error localizado si falla sin datos. Si existen datos
  previos de la misma sesión, se mantienen durante una actualización.
- Ambas interfaces reutilizan sus tarjetas de biblioteca y abren la ficha del
  elemento pulsado. Los títulos están en recursos compartidos en inglés/español.
- TV reutiliza el grupo y los modificadores de foco existentes. La primera fila
  con tarjetas puede recibir el foco de entrada si las anteriores no tienen
  tarjetas; películas precede a «Próxima» cuando no hay series.
- El borde blanco existente sigue limitado a películas/capítulos; no se añade
  a las nuevas tarjetas de series.

## Archivos principales

- `data/src/androidMain/com/jellycine/data/repository/MediaRepository.kt`
- `phone/src/main/java/com/dimodori/app/ui/components/common/RecentHomeSection.kt`
- `phone/src/main/java/com/dimodori/app/ui/screens/dashboard/home/Dashboard.kt`
- `phone/src/main/java/com/dimodori/app/tv/ui/screens/dashboard/home/Dashboard.kt`
- `phone/src/main/java/com/dimodori/app/tv/ui/screens/dashboard/home/RecentTitlesSection.kt`

## Verificación realizada

`python3 scripts/check-home-recent-titles.py` ejecutó:

- 41 comprobaciones de los métodos de producción del repositorio extraídos
  literalmente, compilados con Kotlin, usando dobles de API HTTP/sesión/DTO.
  Incluyen los parámetros usados para ambos servidores, filtrado, eliminación
  de duplicados, orden recibido, límite, resultados vacíos, error independiente,
  cancelación y falta de sesión.
- 6 contratos de código para orden de filas, consultas separadas y acotadas,
  estados de carga/error/vacío, conexiones del foco TV, traducciones y ruta
  común del cliente de servidor.

También aprobaron los contratos existentes de borde de foco TV (3), selector
de pistas (6), entrada TV (6) y PiP (8). Se analizaron 6 archivos Kotlin
nuevos/modificados con Kotlin PSI sin errores de sintaxis.

Estas comprobaciones **no son una compilación Android ni pruebas visuales o
contra servidores reales**. Se intentó compilar Debug/Release y ejecutar
pruebas unitarias con Gradle, pero se detuvo con `SDK location not found`.
No hay `adb` disponible. Quedan pendientes la compilación Android, la revisión
táctil/D-pad en dispositivo y la comprobación con datos reales de Emby/Jellyfin.

No se modificaron versiones, firma, manifiestos, dependencias, servidores,
autenticación o reproductores. No se generó ni publicó APK/AAB.
