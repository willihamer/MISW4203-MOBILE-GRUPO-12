# HU10 — Agregar artistas favoritos

Como coleccionista quiero agregar mis artistas favoritos para que los demás usuarios conozcan mis gustos musicales.

## Comportamiento

En el detalle de un músico (P05) o de una banda (P05b), el coleccionista ve un botón con un corazón junto al nombre del artista. El corazón aparece en contorno si el artista aún no es favorito y relleno si ya lo es. En modo visitante el botón no se muestra.

Al abrir el detalle se consulta si el artista ya está entre los favoritos del coleccionista activo. Al tocar el corazón se envía la solicitud. Mientras está en curso, el botón no acepta otro toque. Si se guarda con éxito, el corazón queda relleno, aparece la confirmación «Artista agregado a tus favoritos» y el artista se muestra entre los favoritos del detalle del coleccionista (HU06). Si el artista ya era favorito, se indica con «Este artista ya está en tus favoritos» y no se vuelve a agregar. Si falla la conexión o el servidor, el corazón sigue en contorno, se muestra el motivo y se puede reintentar.

## Accesibilidad

Se siguen los criterios de la [revisión de accesibilidad del Sprint 3](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki/Accesibilidad-Sprint-3):

- El botón usa el estilo `Widget.Vinilos.IconButton`, con un área táctil de 48 dp.
- TalkBack lee la acción en la descripción («Agregar a mis favoritos», «Está en mis favoritos», «Agregando a mis favoritos…») y el estado aparte («Favorito» o «No favorito»).
- La confirmación y los errores se muestran en un Snackbar, que TalkBack anuncia.
- El corazón activo usa `vn_tertiary` (`#EC4899`) sobre el fondo oscuro, con un contraste mayor que 3:1 para un elemento gráfico.

## Contrato y arquitectura

| Endpoint | Uso |
|---|---|
| `GET /collectors/{collectorId}/performers` | Favoritos del coleccionista, para saber si el artista ya lo es. |
| `POST /collectors/{collectorId}/musicians/{musicianId}` | Agrega un músico a los favoritos. Responde 200 con el artista. |
| `POST /collectors/{collectorId}/bands/{bandId}` | Agrega una banda a los favoritos. Responde 200 con el artista. |

Los POST no llevan cuerpo. El identificador del coleccionista proviene del perfil activo en `ProfileRepository`.

BackVynils guarda músicos y bandas en una misma tabla de artistas, así que sus ids no se repiten y basta compararlos por id. Además, el backend no rechaza un favorito repetido. Por eso el repositorio verifica antes del POST, primero en el detalle del coleccionista en caché y luego en el servidor. Si el artista ya está, lanza `AppError.AlreadyFavorite` sin enviar el POST. Al guardar, agrega el artista a los favoritos en caché para que el detalle del coleccionista lo muestre sin volver a la red.

| Capa / paquete | Elementos | Responsabilidad |
|---|---|---|
| `ui.artist.detail` | `ArtistDetailFragment` | Mostrar el corazón solo en modo coleccionista, con su descripción y estado para TalkBack, y la confirmación en un Snackbar. |
| `ui.artist.detail` | `ArtistDetailViewModel`, `FavoriteUiState` | Consultar si el artista es favorito, evitar envíos repetidos y exponer el mensaje que la vista muestra una sola vez. |
| `data.repository` | `CollectorRepository`, `CollectorRepositoryImpl`, `ProfileRepository` | Detectar duplicados, agregar el favorito, actualizar la caché de HU06 y proveer el perfil activo. |
| `data.network` | `NetworkServiceAdapter`, `VinilosApi`, `AppError` | Consumir los endpoints y traducir las fallas a errores de la app. |

## Inventario de pruebas

| Caso | Comprobación | Implementación |
|---|---|---|
| PE-HU10-01 | El corazón no se muestra en modo visitante. | `HU10AddFavoriteArtistTest.visitanteNoVeLaOpcion` |
| PE-HU10-02 | Un músico se agrega con `POST /collectors/{id}/musicians/{id}` y el corazón queda relleno. | `HU10AddFavoriteArtistTest.agregaUnMusicoAFavoritos` |
| PE-HU10-03 | Una banda se agrega con `POST /collectors/{id}/bands/{id}`. | `HU10AddFavoriteArtistTest.agregaUnaBandaAFavoritos` |
| PE-HU10-04 | Si el artista ya es favorito, se indica y no se envía el POST. | `HU10AddFavoriteArtistTest.indicaSiYaEsFavoritoYNoLoVuelveAAgregar` |
| PE-HU10-05 | El artista agregado aparece entre los favoritos del coleccionista (HU06). | `HU10AddFavoriteArtistTest.elFavoritoApareceEnElDetalleDelColeccionista` |
| PE-HU10-06 | Un error del servidor deja el corazón en contorno y permite reintentar. | `HU10AddFavoriteArtistTest.unErrorDelServidorPermiteReintentar` |

Las pruebas de interfaz usan `MockWebServer`. Las pruebas unitarias en `ArtistDetailViewModelTest`, `CollectorRepositoryTest` y `NetworkServiceAdapterTest` cubren:

- el modo visitante;
- la consulta inicial y su falla;
- el endpoint de cada tipo de artista;
- los duplicados en caché y en el servidor;
- la actualización de la caché de HU06;
- los errores de red y del servidor, con el reintento.

Para ejecutar:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=co.edu.uniandes.vinilos.HU10AddFavoriteArtistTest"
```

Los reportes se generan en `app/build/reports/` y `app/build/outputs/androidTest-results/`.
