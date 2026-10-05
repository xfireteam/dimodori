# Diagnóstico de visibilidad de DIMODORI en Android TV

## Resultado

No se confirmó una causa única de la ausencia en la tienda de la NVIDIA SHIELD.
No se encontró un bloqueo evidente en la configuración fuente de Android TV y
no se modificaron el manifiesto, las arquitecturas, la identidad ni la versión.

Se confirmó una inconsistencia de la **ficha pública de Google Play para Perú**:
el título es «Dimodori», pero la descripción todavía dice «Wardabi» y no menciona
«Android TV». La guía oficial de distribución pide mencionar «Android TV» en la
descripción. Esto debe corregirse, pero **no demuestra por sí solo que la app haya
sido rechazada ni que ese texto sea la causa del filtrado en la SHIELD**.

La aprobación específica de TV y la elegibilidad del **archivo efectivamente
publicado** siguen sin verificarse. «Activo» en factores de forma, producción
activa y compatibilidad del catálogo general no sustituyen esas comprobaciones.

## Evidencia revisada

### Play Console y dispositivo

- Las capturas muestran Android TV activo, usando el mismo segmento y artefactos
  que móvil; producción muestra la versión 1.1.4 activa.
- Perú está incluido en los países de producción.
- El catálogo mostrado marca compatibles tres modelos NVIDIA SHIELD TV,
  incluido Pro, con Android 11.
- El usuario confirmó que la SHIELD aparece como destino de instalación para
  otras apps desde la misma cuenta de Play, pero no para DIMODORI.
- El usuario confirmó que la ficha tiene captura y banner de TV y no ve avisos
  de revisión pendiente ni rechazo. No se obtuvo un estado explícito de
  aprobación de calidad para TV.

### Proyecto

- Identidad compartida: `com.dimodori.app`.
- La actividad de TV tiene `MAIN` y `LEANBACK_LAUNCHER`, y está exportada.
- `android.software.leanback` es opcional para mantener compatibilidad con móvil.
- `android.hardware.touchscreen` no es obligatorio.
- La aplicación declara un banner; el PNG integrado mide 320 × 180 y contiene
  el nombre DIMODORI.
- `minSdk = 27` corresponde a Android 8.1, inferior a Android 11 en el catálogo
  mostrado para SHIELD.
- El módulo core declara `armeabi-v7a`, `arm64-v8a`, `x86` y `x86_64`.
  Esta declaración **no certifica** que todas las bibliotecas nativas del AAB
  publicado estén presentes para cada ABI o cumplan la alineación exigida.
- Codemagic está configurado para ejecutar `:phone:assembleRelease` y
  `:phone:bundleRelease` y recoger APK y AAB.
- El código fuente define versión visible 1.1.4 y código interno 29.

### Límites

No hay un AAB ni APK disponible en este workspace, ni manifiesto fusionado de
release, ni SDK Android configurado. No se compiló otra versión ni se validó el
binario publicado. No hay acceso conectado a la cuenta de Play Console. No se
recibió evidencia de la compatibilidad filtrada por la versión publicada.

Por ello, no se pueden dar por verificadas la matriz real de ABIs, las funciones
obligatorias aportadas por dependencias, la aprobación de TV ni la compatibilidad
con páginas de memoria de 16 KB. La guía de TV indica requisitos de 64 bits y
16 KB desde el 1 de agosto de 2026; deben comprobarse sobre el binario, no solo
sobre Gradle. Tampoco se ha demostrado que alguno de ellos sea el bloqueo actual.

## Acciones concretas

1. **Corregir la descripción de Play**, tanto en la ficha predeterminada como en
   las traducciones aplicables a Perú. Sustituir «Wardabi» por DIMODORI e incluir
   «Android TV». Enviar el cambio de ficha para revisión y comprobar que se
   publica; guardarlo como borrador no actualiza la ficha pública. Esta corrección
   de texto no exige por sí misma subir otro AAB ni incrementar versiones.
2. **Obtener el resultado explícito de revisión de TV**. Consultar la revisión
   de Android TV y las notificaciones de Google Play: pendiente, aprobada o no
   aprobada. La guía oficial menciona la sección Android TV de «Precios y
   distribución»; la ubicación puede variar en la interfaz actual. No interpretar
   ausencia de avisos o factor de forma activo como aprobación.
3. **Comprobar la versión distribuida**, no solo el catálogo general. En
   **Prueba y lanza → Versiones y paquetes más recientes**, seleccionar 1.1.4
   y comprobar el código interno; revisar **Dispositivos → Ver catálogo de
   dispositivos** para el modelo exacto de SHIELD. Revisar también **Entrega**
   y las variantes generadas. Si la interfaz conserva el nombre «Explorador de
   App Bundle», usar esa vista equivalente.
4. **Si hay un problema del paquete**, obtener el AAB que se subió a Play
   (artefacto de Codemagic) o los APK generados por Play para esa SHIELD desde
   **Descargas**. Inspeccionar el manifiesto final, versión, ABIs y bibliotecas
   nativas. Corregir únicamente la restricción demostrada; no crear una segunda
   app, exigir Leanback en móvil o cambiar a un segmento exclusivo a ciegas.
5. **Si TV está aprobada y la versión figura admitida para esa SHIELD**, usar el
   canal de ayuda de Google Play para problemas de visibilidad. Adjuntar el
   paquete, versión/código interno, modelo exacto y capturas de aprobación,
   compatibilidad por versión y ausencia como destino de instalación. No incluir
   credenciales ni enlaces privados firmados.
6. **Verificar el resultado** desde la misma cuenta: la SHIELD debe aparecer
   como destino en la ficha de DIMODORI y la app debe poder instalarse desde la
   tienda del dispositivo. La búsqueda sola no basta para verificar distribución;
   sus resultados dependen de factores de ranking. No se garantiza un plazo de
   indexación ni se afirma que la incidencia esté resuelta sin esa prueba.

## Texto sugerido para la ficha

**Descripción breve**

DIMODORI: tus bibliotecas de Jellyfin y Emby en móvil, tablet y Android TV.

**Descripción completa**

DIMODORI se conecta a tus bibliotecas de Jellyfin y Emby para explorar y reproducir
tu contenido desde teléfonos, tablets y Android TV.

En Android TV, utiliza la interfaz adaptada al televisor y navega con el mando
para abrir películas, series y episodios de tus bibliotecas.

Configura la conexión a tu servidor y accede al contenido disponible en tu cuenta.

## Fuentes

- [Ficha pública de DIMODORI, español y Perú](https://play.google.com/store/apps/details?id=com.dimodori.app&hl=es_PE&gl=PE)
- [Distribución para Android TV: ficha, revisión y aprobación](https://developer.android.com/training/tv/publishing/distribute?hl=es-419)
- [Versiones y paquetes: compatibilidad por versión y descargas](https://support.google.com/googleplay/android-developer/answer/9844279?hl=es-419)
- [Problemas de visibilidad y descubrimiento en Play](https://support.google.com/googleplay/android-developer/answer/9042516?hl=es-419)
