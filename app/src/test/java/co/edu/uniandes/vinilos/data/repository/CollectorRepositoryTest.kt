package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class CollectorRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeVinilosApi()
    private val repository: CollectorRepository = CollectorRepositoryImpl(
        NetworkServiceAdapter(api),
        mainDispatcherRule.dispatcher,
    )

    @Test
    fun `combina el contacto y los favoritos con los albumes de la coleccion`() = runTest {
        val detail = repository.getCollector(100)

        assertEquals("Manolo Bellon", detail.name)
        assertEquals("3502457896", detail.telephone)
        assertEquals(listOf("Rubén Blades Bellido de Luna", "Queen"), detail.favoritePerformers.map { it.name })
        assertEquals(listOf("Buscando América", "A Night at the Opera"), detail.albums.map { it.album.name })
        assertEquals(1, api.collectorDetailCalls)
        assertEquals(1, api.collectorAlbumsCalls)
    }

    @Test
    fun `usa cache por id y forceRefresh vuelve a consultar ambos endpoints`() = runTest {
        repository.getCollector(100)
        repository.getCollector(100)
        assertEquals(1, api.collectorDetailCalls)

        repository.getCollector(7)
        assertEquals(2, api.collectorDetailCalls)

        repository.getCollector(100, forceRefresh = true)
        assertEquals(3, api.collectorDetailCalls)
        assertEquals(3, api.collectorAlbumsCalls)
    }

    @Test
    fun `un coleccionista sin favoritos ni albumes da listas vacias`() = runTest {
        val detail = repository.getCollector(7)

        assertTrue(detail.favoritePerformers.isEmpty())
        assertTrue(detail.albums.isEmpty())
    }

    @Test
    fun `propaga el 404 del backend como AppError Server`() = runTest {
        try {
            repository.getCollector(999)
            fail("Se esperaba AppError.Server")
        } catch (e: AppError.Server) {
            assertEquals(404, e.code)
        }
    }

    @Test
    fun `si falla la consulta de albumes no guarda un detalle incompleto`() = runTest {
        api.collectorAlbums = { throw IOException() }
        try {
            repository.getCollector(100)
            fail("Se esperaba AppError.NoConnection")
        } catch (e: AppError.NoConnection) {
            // esperado
        }

        api.collectorAlbums = FakeVinilosApi().collectorAlbums
        assertEquals(2, repository.getCollector(100).albums.size)
    }
}
