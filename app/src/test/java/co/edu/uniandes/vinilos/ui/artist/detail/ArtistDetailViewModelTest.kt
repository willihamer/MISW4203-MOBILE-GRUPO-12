package co.edu.uniandes.vinilos.ui.artist.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.data.repository.ArtistRepository
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.testutil.SampleData
import co.edu.uniandes.vinilos.ui.common.UiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ArtistDetailViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeVinilosApi()

    private fun createViewModel(id: Int, type: ArtistType) = ArtistDetailViewModel(
        ArtistRepository(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher),
        SavedStateHandle(
            mapOf(
                ArtistDetailViewModel.ARG_ARTIST_ID to id,
                ArtistDetailViewModel.ARG_ARTIST_TYPE to type.name,
            ),
        ),
    )

    private fun ArtistDetailViewModel.artist(): Artist {
        val state = state.value
        check(state is UiState.Success<*>) { "Se esperaba Success y llegó $state" }
        return state.data as Artist
    }

    @Test
    fun `carga el detalle del musico con sus albumes`() {
        val artist = createViewModel(100, ArtistType.MUSICIAN).artist()

        assertTrue(artist is Artist.Musician)
        assertEquals("Rubén Blades Bellido de Luna", artist.name)
        assertEquals(listOf("Buscando América"), artist.albums.map { it.name })
    }

    @Test
    fun `carga el detalle de la banda con sus albumes e integrantes`() {
        val artist = createViewModel(101, ArtistType.BAND).artist() as Artist.Band

        assertEquals("Queen", artist.name)
        assertEquals(listOf("A Night at the Opera"), artist.albums.map { it.name })
        assertEquals(listOf("Freddie Mercury"), artist.musicians.map { it.name })
    }

    @Test
    fun `sin conexion produce Error con su mensaje`() {
        api.musician = { throw IOException() }

        assertEquals(UiState.Error(R.string.error_no_connection), createViewModel(100, ArtistType.MUSICIAN).state.value)
    }

    @Test
    fun `un artista inexistente produce el mensaje de no encontrado`() {
        assertEquals(UiState.Error(R.string.error_artist_not_found), createViewModel(999, ArtistType.BAND).state.value)
    }

    @Test
    fun `reintentar despues de un error carga el artista`() {
        api.band = { throw IOException() }
        val viewModel = createViewModel(101, ArtistType.BAND)
        assertTrue(viewModel.state.value is UiState.Error)

        api.band = { SampleData.queenDetail }
        viewModel.load(forceRefresh = true)

        assertEquals("Queen", viewModel.artist().name)
    }
}
