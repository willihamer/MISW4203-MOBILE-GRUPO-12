package co.edu.uniandes.vinilos.data.repository

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
}
