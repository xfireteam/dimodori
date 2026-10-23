# Borde blanco de foco en Android TV

## Alcance

El borde aparece únicamente en imágenes de tarjetas de tipo `Movie` o `Episode`
cuando su tarjeta tiene el foco del mando. Se admiten las variantes de
mayúsculas/minúsculas usadas por el servidor y Seerr (`movie`).

No se añade a series (`Series`/`tv`), temporadas, colecciones, personas,
bibliotecas, esqueletos, botones ni menús. No representa un favorito o una
selección persistente.

## Implementación

`phone/src/main/java/com/dimodori/app/tv/ui/focus/` contiene la política de tipos,
el observador de foco y la decoración. El trazo blanco es de 2 dp, sigue la
forma de la imagen y se dibuja al final, dentro de sus límites. No modifica
medidas, espaciado, escalado, callbacks, teclas ni destinos de foco.

Se cubren 14 renderizadores propios de TV:

- Inicio: `ContinueWatchingCard`, `LibraryItemCard` (también sus reutilizaciones
  en bibliotecas y descubrimiento).
- Ver todo: `RecentlyAddedCard`, `PosterCard`.
- Búsqueda: `SearchResultCard`, `EpisodeResultCard`, `SearchSeerrResultCard`.
- Favoritos: `FavoriteItemCard`.
- Premios: `AwardTvCard`.
- Detalle: `EpisodeCard`, `EpisodePreviewCard`, `SeasonEpisodeRow`,
  `BoxSetMovieCard`, `PersonTitleCard` (títulos, no retratos de personas).

Además, `SimilarTitleCard` y `SeerrRecommendationCard` del componente compartido
`SimilarItems.kt` aceptan una decoración opcional. Solo
`tv/ui/screens/detail/Recommendations.kt` la activa. Su valor predeterminado es
`null`: los consumidores móviles conservan sus modificadores y no incorporan
el observador de foco.

Los textos externos a la imagen quedan fuera del borde. Se conservan las
animaciones, las imágenes y los indicadores existentes.

## Verificación realizada

- 39 comprobaciones ejecutadas contra la política Kotlin real, sin SDK:
  tipos admitidos/excluidos, pérdida y cambio de foco.
- 3 contratos de código: dibujo sin nuevos controles ni cambios de tamaño,
  conexiones de los renderizadores y activación exclusiva en recomendaciones TV.
- 20 contratos existentes aprobados: selector de pistas (6), entrada TV (6)
  y Picture-in-Picture (8).
- 16 archivos Kotlin nuevos/modificados analizados con Kotlin PSI sin errores
  de sintaxis. Esto **no sustituye** una compilación Android.
- De 332 archivos Android de código/configuración existentes registrados antes
  del cambio, 320 permanecen idénticos byte a byte. Solo cambian 11 archivos TV
  y el componente compartido con activación opcional.

Se añadió `TvContentFocusOutlineTest.kt` para verificar en un dispositivo:
movimiento con D-pad, aparición/desaparición del borde, exclusión de series,
dimensiones estables y una sola activación por pulsación.

**Pendiente:** compilar Debug/Release y androidTest, ejecutar la prueba nativa
y revisar visualmente las pantallas reales en TV. Gradle alcanza la
configuración del proyecto, pero se detiene con `SDK location not found`.
No hay SDK ni `adb` disponibles en este entorno; la prueba nativa no se ejecutó.

## Sin cambios de distribución

No se modificaron versión, identidad, manifiestos, firma, dependencias,
Codemagic, reproductores, PiP ni selectores. No se generó APK/AAB ni se publicó.
