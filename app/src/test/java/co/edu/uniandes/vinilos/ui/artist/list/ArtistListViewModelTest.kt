package co.edu.uniandes.vinilos.ui.artist.list

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ArtistListViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeVinilosApi(
        musicians = { SampleData.musicians },
        bands = { SampleData.bands },
    )

    private fun createViewModel() = ArtistListViewModel(
        ArtistRepository(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher),
    )

    private fun ArtistListViewModel.artistNames(): List<String> {
        val state = state.value
        check(state is UiState.Success<*>) { "Se esperaba Success y llegó $state" }
        @Suppress("UNCHECKED_CAST")
        return (state.data as List<Artist>).map { it.name }
    }

    @Test
    fun `carga el listado de artistas al crearse`() {
        val viewModel = createViewModel()
        assertEquals(
            listOf("Fania All-Stars", "Queen", "Rubén Blades Bellido de Luna", "Willie Colón"),
            viewModel.artistNames(),
        )
    }

    @Test
    fun `listado vacio produce Empty`() {
        api.musicians = { emptyList() }
        api.bands = { emptyList() }
        assertEquals(UiState.Empty, createViewModel().state.value)
    }

    @Test
    fun `sin conexion produce Error con su mensaje`() {
        api.musicians = { throw IOException() }
        assertEquals(UiState.Error(R.string.error_no_connection), createViewModel().state.value)
    }

    @Test
    fun `reintentar fuerza la red y recupera los artistas`() {
        api.musicians = { throw IOException() }
        val viewModel = createViewModel()

        api.musicians = { SampleData.musicians }
        viewModel.load(forceRefresh = true)

        assertEquals(4, viewModel.artistNames().size)
    }

    @Test
    fun `la segunda carga sin forzar usa la cache`() {
        val viewModel = createViewModel()
        viewModel.load()
        assertEquals(1, api.musicianCalls)
        assertEquals(1, api.bandCalls)
    }

    @Test
    fun `filtra por tipo musicos`() {
        val viewModel = createViewModel()
        viewModel.onTypeSelected(ArtistType.MUSICIAN)

        assertEquals(
            listOf("Rubén Blades Bellido de Luna", "Willie Colón"),
            viewModel.artistNames(),
        )
    }

    @Test
    fun `filtra por tipo bandas`() {
        val viewModel = createViewModel()
        viewModel.onTypeSelected(ArtistType.BAND)

        assertEquals(
            listOf("Fania All-Stars", "Queen"),
            viewModel.artistNames(),
        )
    }

    @Test
    fun `vuelve a Todos al seleccionar null`() {
        val viewModel = createViewModel()
        viewModel.onTypeSelected(ArtistType.MUSICIAN)
        assertEquals(2, viewModel.artistNames().size)

        viewModel.onTypeSelected(null)
        assertEquals(4, viewModel.artistNames().size)
    }

    @Test
    fun `busca por nombre sin importar tildes ni mayusculas`() {
        val viewModel = createViewModel()
        viewModel.onQueryChanged("ruben")

        assertEquals(listOf("Rubén Blades Bellido de Luna"), viewModel.artistNames())
    }

    @Test
    fun `sin coincidencias da Success con lista vacia y no Empty`() {
        val viewModel = createViewModel()
        viewModel.onQueryChanged("zzz")

        assertEquals(UiState.Success(emptyList<Artist>()), viewModel.state.value)
    }

    @Test
    fun `busqueda y filtro de tipo se combinan`() {
        val viewModel = createViewModel()
        viewModel.onTypeSelected(ArtistType.BAND)
        viewModel.onQueryChanged("queen")

        assertEquals(listOf("Queen"), viewModel.artistNames())

        // Buscar un músico con filtro de bandas activado debe retornar vacío
        viewModel.onQueryChanged("ruben")
        assertEquals(emptyList<String>(), viewModel.artistNames())
    }
}
