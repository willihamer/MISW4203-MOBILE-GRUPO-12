package co.edu.uniandes.vinilos.data.network

import co.edu.uniandes.vinilos.data.model.Genre
import co.edu.uniandes.vinilos.data.model.NewAlbum
import co.edu.uniandes.vinilos.data.model.RecordLabel
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
