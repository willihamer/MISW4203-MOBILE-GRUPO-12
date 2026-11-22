# HU09 — Comentar un álbum

Como coleccionista quiero comentar un álbum para dar a conocer mi opinión.

## Comportamiento

Desde el detalle del álbum, el coleccionista abre «Comentar». El formulario muestra el álbum y el perfil activo, permite seleccionar entre una y cinco estrellas y escribir una reseña. Ambos campos son obligatorios. El visitante puede leer los comentarios, pero no publicarlos.

Al publicar, se deshabilitan los campos y las acciones de cierre hasta recibir la respuesta. Si el servidor rechaza la solicitud o falla la conexión, se conserva el borrador y se permite reintentar. Al guardar correctamente, se cierra el formulario, se confirma la publicación y se agrega el comentario al detalle. Cancelar antes del envío no escribe datos.

El formulario sigue la pantalla P10 del prototipo: hoja inferior, calificación, reseña, perfil de publicación y acciones Cancelar/Publicar. El borrador se conserva mediante `SavedStateHandle`.

## Contrato y arquitectura

`POST /albums/{id}/comments` recibe:

```json
{
  "description": "Excelente álbum",
  "rating": 4,
  "collector": { "id": 7 }
}
```

La respuesta HTTP 200 contiene el comentario creado. El identificador del coleccionista proviene del perfil activo en `ProfileRepository`. El formulario admite calificaciones enteras de 1 a 5.

| Capa / paquete | Elementos | Responsabilidad |
|---|---|---|
| `ui.album.detail` | `AlbumDetailFragment` | Mostrar comentarios y abrir el formulario según el perfil. |
| `ui.album.comment` | `AddCommentBottomSheet`, `AddCommentViewModel` | Presentación, borrador, validaciones y estado del envío. |
| `data.repository` | `AlbumRepository`, `AlbumRepositoryImpl`, `ProfileRepository` | Guardar comentarios, actualizar la caché y proporcionar el perfil activo. |
| `data.network` | `NetworkServiceAdapter`, `VinilosApi`, DTO y mappers | Consumir REST y traducir respuestas y errores. |
| `data.model` | `Album`, `Comment`, `NewComment` | Representar el detalle, sus comentarios y los datos de publicación. |

La vista comunica el éxito mediante un resultado de Fragment. El repositorio agrega la respuesta a la caché del detalle e invalida la del catálogo; el ViewModel del detalle vuelve a leer la caché. `GET /albums/{id}` ya incluye los comentarios, de modo que también aparecen al volver a consultar el álbum. Las llamadas se ejecutan fuera del hilo principal y se conserva el mecanismo existente de espera para Espresso.

## Inventario de pruebas

| Caso | Comprobación | Implementación |
|---|---|---|
| PE-HU09-01 | Visitante puede leer, pero no publicar. | `HU09AddCommentTest.visitantePuedeLeerPeroNoPublicar` |
| PE-HU09-02 | Reseña y calificación obligatorias; no hay POST con datos vacíos. | `HU09AddCommentTest.exigeLosDosCamposSinEnviarSolicitud` |
| PE-HU09-03 | Publicación con el coleccionista activo y actualización del detalle. | `HU09AddCommentTest.publicaConElColeccionistaActivoYActualizaElDetalle` |
| PE-HU09-04 | Error del servidor conserva el borrador y permite reintentar. | `HU09AddCommentTest.conservaElBorradorAnteErrorYPermiteReintentar` |
| PE-HU09-05 | Cancelar no publica. | `HU09AddCommentTest.cancelarNoPublica` |
| PE-HU09-06 | Recrear la actividad conserva la reseña multilínea, la calificación y el perfil sin publicar. | `HU09AddCommentTest.recrearLaActividadConservaElBorrador` |
| PE-HU09-07 | El detalle de un álbum sin comentarios muestra el mensaje de estado vacío. | `HU09AddCommentTest.albumSinComentariosMuestraEstadoVacio` |

Las pruebas de interfaz usan `MockWebServer`. El caso de publicación comprueba el cuerpo del POST, la calificación visible en el detalle y la conservación de la reseña anterior. Las diez pruebas unitarias de `AddCommentViewModelTest` cubren validación, perfil vigente, duplicados, errores y reintento, restauración del borrador y comportamiento de la caché ante éxito y fallo.

Para ejecutar:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=co.edu.uniandes.vinilos.HU09AddCommentTest"
```

Los reportes se generan en `app/build/reports/` y `app/build/outputs/androidTest-results/`.

## Resultados de pruebas

| Verificación | Resultado |
|---|---|
| APK de depuración y APK de pruebas | Ambos compilaron. |
| Pruebas unitarias del proyecto | 118 aprobadas, incluidas las 10 de HU09. |
| Android lint de depuración | 0 incidencias en el XML. |
| Espresso HU09 en API 21 | 7 de 7 aprobadas, incluida la selección de estrellas por teclado y por toque. |
| Espresso HU09 en API 37 | 7 de 7 aprobadas. |
| Regresión del coleccionista en API 37 | 6 de 6 aprobadas en HU07 y 7 de 7 en HU08. |

[Formulario tras un error del servidor](https://raw.githubusercontent.com/wiki/willihamer/MISW4203-MOBILE-GRUPO-12/evidencias/hu09/hu09-reintento.png)
