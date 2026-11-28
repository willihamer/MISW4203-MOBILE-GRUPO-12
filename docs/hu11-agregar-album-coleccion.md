# HU11 — Agregar álbum a coleccionista

Como coleccionista quiero agregar un álbum para poder venderlo o intercambiarlo.

## Comportamiento

Desde el detalle del álbum, el coleccionista abre «Mi colección». El formulario muestra el álbum con sus artistas y permite registrar el precio en pesos colombianos (COP) y el estado (`Activo` o `Inactivo`). Ambos campos son obligatorios y el precio debe ser un número mayor que cero. La opción no está disponible en modo visitante.

Al enviar, se deshabilitan los campos y las acciones de cierre hasta recibir la respuesta. Si el álbum ya se encuentra en la colección del coleccionista activo, se informa en el formulario y no se vuelve a agregar. Si el servidor rechaza la solicitud o falla la conexión, se conserva el borrador y se permite reintentar. Al guardar correctamente, se cierra el formulario, se confirma la acción y el álbum aparece en la colección del coleccionista (HU06). Cancelar antes del envío no escribe datos.

El formulario sigue la pantalla P11 del prototipo: hoja inferior con precio, estado Activo/Inactivo y acciones Cancelar/Agregar. El borrador se conserva mediante `SavedStateHandle`.

## Contrato y arquitectura

`POST /collectors/{collectorId}/albums/{albumId}` recibe:

```json
{
  "price": 85000,
  "status": "Active"
}
```

La respuesta HTTP 200 contiene el `CollectorAlbum` creado junto con el álbum asociado. El identificador del coleccionista proviene del perfil activo en `ProfileRepository`. Como BackVynils no rechaza asociaciones repetidas en `POST /collectors/{collectorId}/albums/{albumId}`, el repositorio verifica primero si el álbum ya figura en la colección en caché o mediante `GET /collectors/{collectorId}/albums/{albumId}` y, en ese caso, lanza `AppError.AlreadyInCollection` sin ejecutar el `POST`.

| Capa / paquete | Elementos | Responsabilidad |
|---|---|---|
| `ui.album.detail` | `AlbumDetailFragment` | Mostrar el botón «Mi colección» solo en modo coleccionista y confirmar el guardado. |
| `ui.album.collection` | `AddCollectorAlbumBottomSheet`, `AddCollectorAlbumViewModel` | Presentación, borrador, validaciones de precio y estado y estado del envío. |
| `ui.collector.detail` | `CollectorDetailFragment`, `CollectorDetailViewModel` | Refrescar la colección desde la caché al volver al detalle del coleccionista (HU06). |
| `data.repository` | `CollectorRepository`, `CollectorRepositoryImpl`, `ProfileRepository` | Evitar duplicados, asociar el álbum al coleccionista, actualizar la caché de HU06 y proveer el perfil activo. |
| `data.network` | `NetworkServiceAdapter`, `VinilosApi`, DTO y mappers | Consumir `GET` y `POST /collectors/{id}/albums/{id}` y traducir respuestas y errores. |
| `data.model` | `CollectorAlbum`, `CollectorAlbumStatus`, `NewCollectorAlbum` | Representar los álbumes de la colección y los datos de registro. |

## Inventario de pruebas

| Caso | Comprobación | Implementación |
|---|---|---|
| PE-HU11-01 | La opción «Mi colección» no se muestra en modo visitante. | `HU11AddCollectorAlbumTest.visitanteNoVeLaOpcion` |
| PE-HU11-02 | Precio y estado obligatorios; no hay POST con datos vacíos. | `HU11AddCollectorAlbumTest.exigePrecioYEstadoSinEnviarSolicitud` |
| PE-HU11-03 | Precio igual o menor que cero muestra error y no envía POST. | `HU11AddCollectorAlbumTest.rechazaPrecioNoPositivo` |
| PE-HU11-04 | Guarda con el coleccionista activo y el álbum aparece en su colección (HU06). | `HU11AddCollectorAlbumTest.agregaYApareceEnLaColeccion` |
| PE-HU11-05 | Si el álbum ya está en la colección, se informa y no se vuelve a agregar. | `HU11AddCollectorAlbumTest.informaSiYaEstaEnLaColeccion` |
| PE-HU11-06 | Error del servidor conserva el borrador y permite reintentar. | `HU11AddCollectorAlbumTest.conservaElBorradorAnteErrorYPermiteReintentar` |
| PE-HU11-07 | Cancelar cierra el formulario sin enviar POST. | `HU11AddCollectorAlbumTest.cancelarNoAgrega` |
| PE-HU11-08 | Recrear la actividad conserva el precio y el estado seleccionados. | `HU11AddCollectorAlbumTest.recrearLaActividadConservaElBorrador` |

Las pruebas de interfaz usan `MockWebServer`. Las pruebas unitarias en `AddCollectorAlbumViewModelTest`, `CollectorRepositoryTest`, `CollectorViewModelsTest`, `NetworkServiceAdapterTest` y `MappersTest` cubren validación de precio y estado, perfil activo, detección de duplicados en caché y en servidor, actualización de la caché de HU06, errores de red, reintento y restauración del borrador.

Para ejecutar:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=co.edu.uniandes.vinilos.HU11AddCollectorAlbumTest"
```

Los reportes se generan en `app/build/reports/` y `app/build/outputs/androidTest-results/`.

## Resultados de pruebas

| Verificación | Resultado |
|---|---|
| APK de depuración y APK de pruebas (`assembleDebug`, `assembleDebugAndroidTest`) | Ambos compilaron. |
| Pruebas unitarias del proyecto (`testDebugUnitTest`) | 133 aprobadas (0 fallos), incluidas las 8 de `AddCollectorAlbumViewModelTest` y las nuevas de repositorio, adaptador y mappers. |
| Android lint de depuración (`lintDebug`) | 0 incidencias en el reporte XML. |

