package co.edu.uniandes.vinilos.ui.album.list

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.Genre
import co.edu.uniandes.vinilos.data.model.NewAlbum
import co.edu.uniandes.vinilos.data.model.RecordLabel
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.data.repository.AlbumRepository
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.testutil.SampleData
import co.edu.uniandes.vinilos.ui.common.UiState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class AlbumListViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeVinilosApi(albums = { SampleData.albums })

    private fun createViewModel() = AlbumListViewModel(
        AlbumRepository(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher),
    )

    private fun AlbumListViewModel.albumNames(): List<String> {
        val state = state.value
        check(state is UiState.Success<*>) { "Se esperaba Success y llegó $state" }
        @Suppress("UNCHECKED_CAST")
        return (state.data as List<Album>).map { it.name }
    }

    @Test
    fun `carga el catalogo al crearse`() {
        val viewModel = createViewModel()
        assertEquals(listOf("Buscando América", "A Night at the Opera"), viewModel.albumNames())
    }

    @Test
    fun `catalogo vacio produce Empty`() {
        api.albums = { emptyList() }
        assertEquals(UiState.Empty, createViewModel().state.value)
    }

    @Test
    fun `sin conexion produce Error con su mensaje`() {
        api.albums = { throw IOException() }
        assertEquals(UiState.Error(R.string.error_no_connection), createViewModel().state.value)
    }

    @Test
    fun `reintentar fuerza la red y recupera el catalogo`() {
        api.albums = { throw IOException() }
        val viewModel = createViewModel()

        api.albums = { SampleData.albums }
        viewModel.load(forceRefresh = true)

        assertEquals(2, viewModel.albumNames().size)
    }

    @Test
    fun `la segunda carga sin forzar usa la cache`() {
        val viewModel = createViewModel()
        viewModel.load()
        assertEquals(1, api.albumCalls)
    }

    @Test
    fun `filtra por genero y vuelve a Todos`() {
        val viewModel = createViewModel()

        viewModel.onGenreSelected(Genre.ROCK)
        assertEquals(listOf("A Night at the Opera"), viewModel.albumNames())

        viewModel.onGenreSelected(null)
        assertEquals(2, viewModel.albumNames().size)
    }

    @Test
    fun `busca por artista sin importar tildes ni mayusculas`() {
        val viewModel = createViewModel()
        viewModel.onQueryChanged("RUBEN")
        assertEquals(listOf("Buscando América"), viewModel.albumNames())
    }

    @Test
    fun `busca por sello discografico`() {
        val viewModel = createViewModel()
        viewModel.onQueryChanged("emi")
        assertEquals(listOf("A Night at the Opera"), viewModel.albumNames())
    }

    @Test
    fun `sin coincidencias da Success con lista vacia y no Empty`() {
        val viewModel = createViewModel()
        viewModel.onQueryChanged("zzz")
        assertEquals(UiState.Success(emptyList<Album>()), viewModel.state.value)
    }

    @Test
    fun `busqueda y genero se combinan`() {
        val viewModel = createViewModel()
        viewModel.onGenreSelected(Genre.SALSA)
        viewModel.onQueryChanged("queen")
        assertEquals(emptyList<String>(), viewModel.albumNames())
    }

    @Test
    fun `refreshIfInvalidated recarga el catalogo cuando se crea un album`() = runTest {
        var currentAlbums = SampleData.albums
        api.albums = { currentAlbums }
        val repository = AlbumRepository(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher)
        val viewModel = AlbumListViewModel(repository)

        assertEquals(1, api.albumCalls)
        viewModel.refreshIfInvalidated()
        assertEquals(1, api.albumCalls)

        repository.createAlbum(
            NewAlbum(
                name = "Siembra",
                cover = "https://example.com/siembra.jpg",
                releaseDate = LocalDate.of(1978, 9, 7),
                description = "Álbum clásico de salsa.",
                genre = Genre.SALSA,
                recordLabel = RecordLabel.FANIA_RECORDS,
            ),
        )
        currentAlbums = currentAlbums + SampleData.buscandoAmerica.copy(id = 200, name = "Siembra")

        viewModel.refreshIfInvalidated()
        assertEquals(2, api.albumCalls)
        assertEquals(listOf("Buscando América", "A Night at the Opera", "Siembra"), viewModel.albumNames())
    }
}
