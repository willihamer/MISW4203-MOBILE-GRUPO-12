package co.edu.uniandes.vinilos.ui.album.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.data.network.dto.TrackDto
import co.edu.uniandes.vinilos.data.repository.AlbumRepository
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.testutil.SampleData
import co.edu.uniandes.vinilos.ui.common.UiState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class AlbumDetailViewModelTest {
    @get:Rule val instantTaskRule = InstantTaskExecutorRule()
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val api = FakeVinilosApi()
    private fun model(id: Int = 100) = AlbumDetailViewModel(
        AlbumRepository(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher),
        SavedStateHandle(mapOf(AlbumDetailViewModel.ARG_ALBUM_ID to id)),
    )
    private fun AlbumDetailViewModel.album() = (state.value as UiState.Success<Album>).data

    @Test fun `carga datos y tracks del album solicitado`() {
        api.album = { SampleData.buscandoAmerica.copy(tracks = listOf(TrackDto(100, "Decisiones", "5:05"))) }
        val album = model().album()
        assertEquals("Buscando América", album.name)
        assertEquals("Elektra", album.recordLabel)
        assertEquals("1984-08-01", album.releaseDate.toString())
        assertEquals("Decisiones", album.tracks.single().name)
        assertEquals("5:05", album.tracks.single().duration)
    }
    @Test fun `album sin tracks mantiene el detalle exitoso`() {
        assertTrue(model().album().tracks.isEmpty())
    }
    @Test fun `album inexistente muestra mensaje especifico`() {
        assertEquals(UiState.Error(R.string.error_album_not_found), model(999).state.value)
    }
    @Test fun `sin conexion permite reintentar`() {
        api.album = { throw IOException() }
        val vm = model()
        assertEquals(UiState.Error(R.string.error_no_connection), vm.state.value)
        api.album = { SampleData.buscandoAmerica }
        vm.load(true)
        assertEquals("Buscando América", vm.album().name)
    }
}
