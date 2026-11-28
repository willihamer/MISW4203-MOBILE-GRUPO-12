package co.edu.uniandes.vinilos.ui.artist.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.data.model.Profile
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.data.network.dto.PerformerDto
import co.edu.uniandes.vinilos.data.repository.ArtistRepositoryImpl
import co.edu.uniandes.vinilos.data.repository.CollectorRepositoryImpl
import co.edu.uniandes.vinilos.data.repository.ProfileRepository
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.testutil.SampleData
import co.edu.uniandes.vinilos.testutil.notFound
import co.edu.uniandes.vinilos.ui.common.UiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    private val profiles = object : ProfileRepository {
        override val profile = MutableLiveData<Profile?>(Profile.Visitor)
        override val current: Profile? get() = profile.value
        override fun selectVisitor() { profile.value = Profile.Visitor }
        override fun selectCollector(collectorId: Int, name: String) {
            profile.value = Profile.CollectorProfile(collectorId, name)
        }
        override fun clear() { profile.value = null }
    }

    private fun createViewModel(id: Int, type: ArtistType) = ArtistDetailViewModel(
        ArtistRepositoryImpl(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher),
        CollectorRepositoryImpl(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher),
        profiles,
        SavedStateHandle(
            mapOf(
                ArtistDetailViewModel.ARG_ARTIST_ID to id,
                ArtistDetailViewModel.ARG_ARTIST_TYPE to type.name,
            ),
        ),
    )

    /** Coleccionista 7 (sin favoritos) o 100 (Rubén Blades y Queen como favoritos). */
    private fun asCollector(collectorId: Int = 7) = profiles.selectCollector(collectorId, "Coleccionista $collectorId")

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

    // HU10 - Agregar artistas favoritos

    @Test
    fun `en modo visitante la opcion de favorito no se muestra ni consulta la red`() {
        val viewModel = createViewModel(102, ArtistType.MUSICIAN)
        viewModel.onFavoriteClicked()

        assertEquals(FavoriteUiState.Hidden, viewModel.favoriteState.value)
        assertEquals(0, api.favoritePerformersCalls)
        assertEquals(0, api.addFavoriteCalls)
    }

    @Test
    fun `en modo coleccionista indica si el artista aun no es favorito`() {
        asCollector(7)

        assertEquals(FavoriteUiState.NotFavorite, createViewModel(102, ArtistType.MUSICIAN).favoriteState.value)
    }

    @Test
    fun `en modo coleccionista indica si el artista ya es favorito`() {
        asCollector(100)

        assertEquals(FavoriteUiState.Favorite, createViewModel(101, ArtistType.BAND).favoriteState.value)
    }

    @Test
    fun `agregar un musico lo marca como favorito y confirma la accion`() {
        asCollector(7)
        val viewModel = createViewModel(102, ArtistType.MUSICIAN)

        viewModel.onFavoriteClicked()

        assertEquals(FavoriteUiState.Favorite, viewModel.favoriteState.value)
        assertEquals(R.string.favorite_added, viewModel.favoriteMessage.value)
        assertEquals("musicians", api.lastFavoriteResource)
        assertEquals(7, api.lastFavoriteCollectorId)
        assertEquals(102, api.lastFavoritePerformerId)
    }

    @Test
    fun `agregar una banda usa el endpoint de bandas`() {
        asCollector(7)
        val viewModel = createViewModel(103, ArtistType.BAND)

        viewModel.onFavoriteClicked()

        assertEquals(FavoriteUiState.Favorite, viewModel.favoriteState.value)
        assertEquals("bands", api.lastFavoriteResource)
        assertEquals(103, api.lastFavoritePerformerId)
    }

    @Test
    fun `si el artista ya es favorito lo indica y no lo vuelve a agregar`() {
        asCollector(100)
        val viewModel = createViewModel(100, ArtistType.MUSICIAN)

        viewModel.onFavoriteClicked()
        viewModel.onFavoriteClicked()

        assertEquals(FavoriteUiState.Favorite, viewModel.favoriteState.value)
        assertEquals(R.string.favorite_already_added, viewModel.favoriteMessage.value)
        assertEquals(0, api.addFavoriteCalls)
    }

    @Test
    fun `un favorito agregado desde otro dispositivo se detecta antes del POST`() {
        asCollector(7)
        var calls = 0
        // La primera consulta (al abrir) no lo trae; la verificación previa al POST, sí
        api.favoritePerformers = {
            calls++
            if (calls == 1) emptyList() else listOf(PerformerDto(102, "Willie Colón", "", birthDate = "1950-04-28"))
        }
        val viewModel = createViewModel(102, ArtistType.MUSICIAN)
        assertEquals(FavoriteUiState.NotFavorite, viewModel.favoriteState.value)

        viewModel.onFavoriteClicked()

        assertEquals(FavoriteUiState.Favorite, viewModel.favoriteState.value)
        assertEquals(R.string.favorite_already_added, viewModel.favoriteMessage.value)
        assertEquals(0, api.addFavoriteCalls)
    }

    @Test
    fun `un error del servidor deja el artista sin marcar y permite reintentar`() {
        asCollector(7)
        api.addFavoriteResponse = { _, _, _ -> throw notFound() }
        val viewModel = createViewModel(102, ArtistType.MUSICIAN)

        viewModel.onFavoriteClicked()

        assertEquals(FavoriteUiState.NotFavorite, viewModel.favoriteState.value)
        assertEquals(R.string.favorite_error, viewModel.favoriteMessage.value)

        api.addFavoriteResponse = FakeVinilosApi().addFavoriteResponse
        viewModel.onFavoriteMessageShown()
        viewModel.onFavoriteClicked()

        assertEquals(FavoriteUiState.Favorite, viewModel.favoriteState.value)
        assertEquals(R.string.favorite_added, viewModel.favoriteMessage.value)
        assertEquals(2, api.addFavoriteCalls)
    }

    @Test
    fun `sin conexion al agregar muestra el mensaje de conexion`() {
        asCollector(7)
        api.addFavoriteResponse = { _, _, _ -> throw IOException() }
        val viewModel = createViewModel(102, ArtistType.MUSICIAN)

        viewModel.onFavoriteClicked()

        assertEquals(FavoriteUiState.NotFavorite, viewModel.favoriteState.value)
        assertEquals(R.string.error_no_connection, viewModel.favoriteMessage.value)
    }

    @Test
    fun `si la consulta inicial falla se permite intentar agregarlo`() {
        asCollector(7)
        api.favoritePerformers = { throw IOException() }

        assertEquals(FavoriteUiState.NotFavorite, createViewModel(102, ArtistType.MUSICIAN).favoriteState.value)
    }

    @Test
    fun `no envia el POST si el artista no se pudo cargar`() {
        asCollector(7)
        api.musician = { throw IOException() }
        val viewModel = createViewModel(102, ArtistType.MUSICIAN)

        viewModel.onFavoriteClicked()

        assertEquals(0, api.addFavoriteCalls)
    }

    @Test
    fun `el mensaje se limpia despues de mostrarse`() {
        asCollector(7)
        val viewModel = createViewModel(102, ArtistType.MUSICIAN)
        viewModel.onFavoriteClicked()

        viewModel.onFavoriteMessageShown()

        assertNull(viewModel.favoriteMessage.value)
    }
}
