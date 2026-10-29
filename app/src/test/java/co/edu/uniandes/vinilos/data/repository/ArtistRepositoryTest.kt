package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.testutil.SampleData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ArtistRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeVinilosApi(
        musicians = { SampleData.musicians },
        bands = { SampleData.bands },
    )
    private val repository = ArtistRepository(
        NetworkServiceAdapter(api),
        mainDispatcherRule.dispatcher,
    )

    @Test
    fun `combina musicos y bandas y los ordena alfabeticamente`() = runTest {
        val artists = repository.getArtists()

        assertEquals(4, artists.size)
        // Orden alfabético esperado: Fania All-Stars, Queen, Rubén Blades Bellido de Luna, Willie Colón
        assertEquals(listOf("Fania All-Stars", "Queen", "Rubén Blades Bellido de Luna", "Willie Colón"), artists.map { it.name })
        assertEquals(listOf(ArtistType.BAND, ArtistType.BAND, ArtistType.MUSICIAN, ArtistType.MUSICIAN), artists.map { it.type })
    }

    @Test
    fun `usa cache en llamadas consecutivas sin forceRefresh`() = runTest {
        repository.getArtists()
        repository.getArtists()

        assertEquals(1, api.musicianCalls)
        assertEquals(1, api.bandCalls)
    }

    @Test
    fun `reintentar con forceRefresh vuelve a consultar la red`() = runTest {
        repository.getArtists()
        repository.getArtists(forceRefresh = true)

        assertEquals(2, api.musicianCalls)
        assertEquals(2, api.bandCalls)
    }

    @Test
    fun `propaga AppError si falla la consulta`() = runTest {
        api.musicians = { throw IOException("sin conexion") }

        try {
            repository.getArtists(forceRefresh = true)
            org.junit.Assert.fail("Se esperaba excepción AppError")
        } catch (e: AppError) {
            assertTrue(e is AppError.NoConnection)
        }
    }

    @Test
    fun `el detalle de un musico se consulta en su endpoint`() = runTest {
        val artist = repository.getArtist(100, ArtistType.MUSICIAN)

        assertTrue(artist is Artist.Musician)
        assertEquals("Rubén Blades Bellido de Luna", artist.name)
        assertEquals(listOf("Buscando América"), artist.albums.map { it.name })
        assertEquals(1, api.musicianDetailCalls)
        assertEquals(0, api.bandDetailCalls)
    }

    @Test
    fun `el detalle de una banda se consulta en su endpoint con sus integrantes`() = runTest {
        val artist = repository.getArtist(101, ArtistType.BAND)

        assertTrue(artist is Artist.Band)
        assertEquals(listOf("Freddie Mercury"), (artist as Artist.Band).musicians.map { it.name })
        assertEquals(1, api.bandDetailCalls)
        assertEquals(0, api.musicianDetailCalls)
    }

    @Test
    fun `el detalle usa cache y reintentar vuelve a consultar la red`() = runTest {
        repository.getArtist(101, ArtistType.BAND)
        repository.getArtist(101, ArtistType.BAND)
        assertEquals(1, api.bandDetailCalls)

        repository.getArtist(101, ArtistType.BAND, forceRefresh = true)
        assertEquals(2, api.bandDetailCalls)
    }

    @Test
    fun `un artista inexistente propaga el error 404 del servidor`() = runTest {
        try {
            repository.getArtist(999, ArtistType.MUSICIAN)
            org.junit.Assert.fail("Se esperaba excepción AppError")
        } catch (e: AppError) {
            assertEquals(404, (e as AppError.Server).code)
        }
    }
}
