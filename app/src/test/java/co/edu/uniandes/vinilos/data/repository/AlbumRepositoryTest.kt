package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Genre
import co.edu.uniandes.vinilos.data.model.NewAlbum
import co.edu.uniandes.vinilos.data.model.RecordLabel
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.SampleData
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AlbumRepositoryTest {
    @Test fun `cache por id evita red y conserva albumes distintos`() = runTest {
        val api = FakeVinilosApi()
        val repo = AlbumRepository(NetworkServiceAdapter(api), UnconfinedTestDispatcher(testScheduler))
        assertEquals("Buscando América", repo.getAlbum(100).name)
        assertEquals("A Night at the Opera", repo.getAlbum(102).name)
        repo.getAlbum(100)
        assertEquals(2, api.albumDetailCalls)
    }
    @Test fun `forzar refresco actualiza detalle almacenado`() = runTest {
        val api = FakeVinilosApi()
        val repo = AlbumRepository(NetworkServiceAdapter(api), UnconfinedTestDispatcher(testScheduler))
        repo.getAlbum(100)
        api.album = { SampleData.buscandoAmerica.copy(description = "Actualizada") }
        assertEquals("Actualizada", repo.getAlbum(100, true).description)
        assertEquals("Actualizada", repo.getAlbum(100).description)
        assertEquals(2, api.albumDetailCalls)
    }
    @Test fun `catalogo no reemplaza la consulta completa del detalle`() = runTest {
        val api = FakeVinilosApi(albums = { SampleData.albums })
        val repo = AlbumRepository(NetworkServiceAdapter(api), UnconfinedTestDispatcher(testScheduler))
        repo.getAlbums()
        repo.getAlbum(100)
        assertEquals(1, api.albumDetailCalls)
    }

    @Test fun `crear un album invalida la cache del catalogo y fuerza nueva consulta`() = runTest {
        var currentAlbums = SampleData.albums
        val api = FakeVinilosApi(albums = { currentAlbums })
        val repo = AlbumRepository(NetworkServiceAdapter(api), UnconfinedTestDispatcher(testScheduler))

        assertEquals(2, repo.getAlbums().size)
        assertTrue(repo.isCatalogCacheValid)
        assertEquals(1, api.albumCalls)

        val newAlbum = NewAlbum(
            name = "Siembra",
            cover = "https://example.com/siembra.jpg",
            releaseDate = LocalDate.of(1978, 9, 7),
            description = "Álbum de salsa.",
            genre = Genre.SALSA,
            recordLabel = RecordLabel.FANIA_RECORDS,
        )
        val created = repo.createAlbum(newAlbum)
        assertFalse(repo.isCatalogCacheValid)
        assertEquals("Siembra", created.name)

        currentAlbums = currentAlbums + SampleData.buscandoAmerica.copy(id = 200, name = "Siembra")
        val refreshed = repo.getAlbums(forceRefresh = false)
        assertEquals(2, api.albumCalls)
        assertEquals(listOf("Buscando América", "A Night at the Opera", "Siembra"), refreshed.map { it.name })
    }
}
