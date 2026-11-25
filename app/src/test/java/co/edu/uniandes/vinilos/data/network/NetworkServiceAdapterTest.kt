package co.edu.uniandes.vinilos.data.network

import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.data.model.CollectorAlbumStatus
import co.edu.uniandes.vinilos.data.model.Genre
import co.edu.uniandes.vinilos.data.model.NewAlbum
import co.edu.uniandes.vinilos.data.model.NewCollectorAlbum
import co.edu.uniandes.vinilos.data.model.NewTrack
import co.edu.uniandes.vinilos.data.model.RecordLabel
import co.edu.uniandes.vinilos.data.network.dto.CollectorAlbumDto
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.SampleData
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate

class NetworkServiceAdapterTest {

    private val api = FakeVinilosApi()
    private val adapter = NetworkServiceAdapter(api)

    @Test
    fun `sin conexion se traduce a NoConnection`() = runTest {
        api.albums = { throw IOException("timeout") }
        assertTrue(captureError { adapter.getAlbums() } is AppError.NoConnection)
    }

    @Test
    fun `un error HTTP se traduce a Server con su codigo`() = runTest {
        api.albums = { throw HttpException(Response.error<Any>(500, "".toResponseBody())) }
        val error = captureError { adapter.getAlbums() }
        assertEquals(500, (error as AppError.Server).code)
    }

    @Test
    fun `cualquier otra falla se traduce a Unknown`() = runTest {
        api.albums = { throw IllegalStateException("JSON inesperado") }
        assertTrue(captureError { adapter.getAlbums() } is AppError.Unknown)
    }

    @Test
    fun `el idling resource queda libre despues de cada llamada`() = runTest {
        api.albums = { throw IOException() }
        captureError { adapter.getAlbums() }
        assertTrue(NetworkIdlingResource.resource.isIdleNow)
    }

    @Test
    fun `getMusicians mapea correctamente la respuesta de la API`() = runTest {
        api.musicians = { SampleData.musicians }
        val musicians = adapter.getMusicians()
        assertEquals(2, musicians.size)
        assertEquals("Rubén Blades Bellido de Luna", musicians.first().name)
    }

    @Test
    fun `getBands mapea correctamente la respuesta de la API`() = runTest {
        api.bands = { SampleData.bands }
        val bands = adapter.getBands()
        assertEquals(2, bands.size)
        assertEquals("Queen", bands.first().name)
    }

    @Test
    fun `getMusicians traduce errores de red a AppError`() = runTest {
        api.musicians = { throw IOException("sin red") }
        assertTrue(captureError { adapter.getMusicians() } is AppError.NoConnection)
    }

    @Test
    fun `getBands traduce errores de red a AppError`() = runTest {
        api.bands = { throw IOException("sin red") }
        assertTrue(captureError { adapter.getBands() } is AppError.NoConnection)
    }

    @Test
    fun `createAlbum envia el DTO y mapea el album creado`() = runTest {
        val input = NewAlbum(
            name = "Siembra",
            cover = "https://example.com/siembra.jpg",
            releaseDate = LocalDate.of(1978, 9, 7),
            description = "Álbum de salsa.",
            genre = Genre.SALSA,
            recordLabel = RecordLabel.FANIA_RECORDS,
        )

        val created = adapter.createAlbum(input)

        assertEquals(1, api.createAlbumCalls)
        assertEquals("Siembra", api.lastCreatedAlbumRequest?.name)
        assertEquals("Fania Records", api.lastCreatedAlbumRequest?.recordLabel)
        assertEquals(200, created.id)
        assertEquals("Siembra", created.name)
        assertEquals(Genre.SALSA, created.genre)
        assertEquals("Fania Records", created.recordLabel)
    }

    @Test
    fun `createAlbum traduce errores HTTP y de red a AppError`() = runTest {
        val input = NewAlbum(
            name = "Siembra",
            cover = "https://example.com/siembra.jpg",
            releaseDate = LocalDate.of(1978, 9, 7),
            description = "Álbum de salsa.",
            genre = Genre.SALSA,
            recordLabel = RecordLabel.FANIA_RECORDS,
        )
        api.createAlbumResponse = { throw HttpException(Response.error<Any>(400, "".toResponseBody())) }
        val error = captureError { adapter.createAlbum(input) }
        assertEquals(400, (error as AppError.Server).code)
    }

    @Test
    fun `addTrack envia nombre y duracion al album indicado y mapea el track creado`() = runTest {
        val created = adapter.addTrack(100, NewTrack(name = "Decisiones", duration = "5:05"))

        assertEquals(1, api.addTrackCalls)
        assertEquals(100, api.lastAddTrackAlbumId)
        assertEquals("Decisiones", api.lastAddTrackRequest?.name)
        assertEquals("5:05", api.lastAddTrackRequest?.duration)
        assertEquals(300, created.id)
        assertEquals("Decisiones", created.name)
        assertEquals("5:05", created.duration)
    }

    @Test
    fun `addTrack traduce un album inexistente y la falta de red a AppError`() = runTest {
        api.addTrackResponse = { _, _ -> throw HttpException(Response.error<Any>(404, "".toResponseBody())) }
        val notFound = captureError { adapter.addTrack(999, NewTrack("Decisiones", "5:05")) }
        assertEquals(404, (notFound as AppError.Server).code)

        api.addTrackResponse = { _, _ -> throw IOException("sin red") }
        assertTrue(captureError { adapter.addTrack(100, NewTrack("Decisiones", "5:05")) } is AppError.NoConnection)
    }

    @Test
    fun `isAlbumInCollection y addAlbumToCollector mapean la respuesta y traducen respuestas sin album a Unknown`() = runTest {
        assertEquals(true, adapter.isAlbumInCollection(100, 100))
        assertEquals(false, adapter.isAlbumInCollection(7, 100))

        val created = adapter.addAlbumToCollector(
            collectorId = 7,
            albumId = 100,
            album = NewCollectorAlbum(price = 85000.0, status = CollectorAlbumStatus.ACTIVE),
        )
        assertEquals(1, api.addCollectorAlbumCalls)
        assertEquals(7, api.lastCollectorAlbumCollectorId)
        assertEquals(100, api.lastCollectorAlbumAlbumId)
        assertEquals(85000.0, api.lastCollectorAlbumRequest?.price ?: 0.0, 0.0)
        assertEquals("Active", api.lastCollectorAlbumRequest?.status)
        assertEquals("Buscando América", created.album.name)
        assertEquals(85000.0, created.price ?: 0.0, 0.0)
        assertEquals("Active", created.status)

        api.addCollectorAlbumResponse = { _, _, req ->
            CollectorAlbumDto(id = 500, price = req.price, status = req.status, album = null)
        }
        assertTrue(
            captureError {
                adapter.addAlbumToCollector(7, 100, NewCollectorAlbum(10.0, CollectorAlbumStatus.ACTIVE))
            } is AppError.Unknown,
        )
    }

    @Test
    fun `getFavoritePerformers mapea los favoritos con su tipo de artista`() = runTest {
        val favorites = adapter.getFavoritePerformers(100)

        assertEquals(listOf(100, 101), favorites.map { it.id })
        assertEquals(listOf(ArtistType.MUSICIAN, ArtistType.BAND), favorites.map { it.type })
        assertEquals(1, api.favoritePerformersCalls)
    }

    @Test
    fun `addFavoritePerformer usa el endpoint de musicos o de bandas segun el tipo`() = runTest {
        adapter.addFavoritePerformer(collectorId = 7, performerId = 100, type = ArtistType.MUSICIAN)
        assertEquals("musicians", api.lastFavoriteResource)
        assertEquals(7, api.lastFavoriteCollectorId)
        assertEquals(100, api.lastFavoritePerformerId)

        adapter.addFavoritePerformer(collectorId = 7, performerId = 101, type = ArtistType.BAND)
        assertEquals("bands", api.lastFavoriteResource)
        assertEquals(101, api.lastFavoritePerformerId)
        assertEquals(2, api.addFavoriteCalls)
    }

    @Test
    fun `addFavoritePerformer traduce las fallas a AppError`() = runTest {
        api.addFavoriteResponse = { _, _, _ -> throw IOException() }
        assertTrue(captureError { adapter.addFavoritePerformer(7, 100, ArtistType.MUSICIAN) } is AppError.NoConnection)

        api.addFavoriteResponse = { _, _, _ -> throw HttpException(Response.error<Any>(404, "".toResponseBody())) }
        assertEquals(404, (captureError { adapter.addFavoritePerformer(7, 999, ArtistType.BAND) } as AppError.Server).code)
    }

    private suspend fun captureError(block: suspend () -> Unit): AppError {
        try {
            block()
        } catch (e: AppError) {
            return e
        }
        fail("Se esperaba un AppError")
        throw IllegalStateException()
    }
}
