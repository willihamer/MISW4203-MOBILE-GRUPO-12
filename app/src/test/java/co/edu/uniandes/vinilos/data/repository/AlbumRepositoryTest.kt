package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.SampleData
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.*
import org.junit.Test

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
}
