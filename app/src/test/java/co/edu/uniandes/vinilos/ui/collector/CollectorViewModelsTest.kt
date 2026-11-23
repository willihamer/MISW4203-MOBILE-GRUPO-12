package co.edu.uniandes.vinilos.ui.collector

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.model.CollectorAlbumStatus
import co.edu.uniandes.vinilos.data.model.CollectorDetail
import co.edu.uniandes.vinilos.data.model.NewCollectorAlbum
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.data.network.dto.CollectorDto
import co.edu.uniandes.vinilos.data.repository.CollectorRepository
import co.edu.uniandes.vinilos.data.repository.CollectorRepositoryImpl
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.ui.collector.detail.CollectorDetailViewModel
import co.edu.uniandes.vinilos.ui.collector.list.CollectorListViewModel
import co.edu.uniandes.vinilos.ui.common.UiState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class CollectorViewModelsTest {
    @get:Rule val instantTaskRule = InstantTaskExecutorRule()
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val records = listOf(
        CollectorDto(7, "José Pérez", "123", "jose@example.com"),
        CollectorDto(42, "Ana López", "456", "ana@example.com"),
    )
    private var calls = 0
    private val api = FakeVinilosApi(collectors = { calls++; records })
    private val repository: CollectorRepository get() = CollectorRepositoryImpl(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher)
    private fun list(saved: SavedStateHandle = SavedStateHandle()) = CollectorListViewModel(repository, saved)
    private fun CollectorListViewModel.names(): List<String> =
        (state.value as UiState.Success<List<Collector>>).data.map { it.name }

    @Test fun `carga nombres y correos desde la API`() {
        val data = (list().state.value as UiState.Success<List<Collector>>).data
        assertEquals(listOf("José Pérez", "Ana López"), data.map { it.name })
        assertEquals(listOf("jose@example.com", "ana@example.com"), data.map { it.email })
    }
    @Test fun `busca sin distinguir mayusculas tildes y espacios externos`() {
        val vm = list()
        vm.onQueryChanged("  JOSE  ")
        assertEquals(listOf("José Pérez"), vm.names())
        vm.onQueryChanged("")
        assertEquals(2, vm.names().size)
        assertEquals(1, calls)
    }
    @Test fun `distingue un listado vacio de una busqueda sin resultados`() {
        val vm = list()
        vm.onQueryChanged("inexistente")
        assertEquals(UiState.Success(emptyList<Collector>()), vm.state.value)
        api.collectors = { emptyList() }
        assertEquals(UiState.Empty, list().state.value)
    }
    @Test fun `recupera la busqueda guardada al recrear el estado`() {
        val saved = SavedStateHandle()
        list(saved).onQueryChanged("ana")
        val restored = SavedStateHandle(saved.keys().associateWith { saved.get<Any>(it) })
        val vm = list(restored)
        assertEquals("ana", vm.query)
        assertEquals(listOf("Ana López"), vm.names())
    }
    @Test fun `recupera una falla de red y conserva la busqueda al reintentar`() {
        val vm = list()
        vm.onQueryChanged("ana")
        api.collectors = { throw IOException() }
        vm.load(true)
        assertEquals(UiState.Error(R.string.error_no_connection), vm.state.value)
        api.collectors = { records }
        vm.load(true)
        assertEquals(listOf("Ana López"), vm.names())
    }
    private fun detail(id: Int, repo: CollectorRepository = repository) =
        CollectorDetailViewModel(repo, SavedStateHandle(mapOf(CollectorDetailViewModel.ARG_COLLECTOR_ID to id)))
    private fun CollectorDetailViewModel.data(): CollectorDetail =
        (state.value as UiState.Success<CollectorDetail>).data

    @Test fun `detalle muestra contacto, artistas favoritos y albumes de la coleccion`() {
        val data = detail(100).data()
        assertEquals("Manolo Bellon", data.name)
        assertEquals("3502457896", data.telephone)
        assertEquals("manollo@caracol.com.co", data.email)
        assertEquals(listOf("Rubén Blades Bellido de Luna", "Queen"), data.favoritePerformers.map { it.name })
        assertEquals(listOf("Buscando América", "A Night at the Opera"), data.albums.map { it.album.name })
    }
    @Test fun `detalle de un coleccionista sin favoritos ni albumes da listas vacias`() {
        val data = detail(7).data()
        assertEquals("José Pérez", data.name)
        assertEquals(emptyList<Any>(), data.favoritePerformers)
        assertEquals(emptyList<Any>(), data.albums)
    }
    @Test fun `volver a abrir el detalle usa la cache del repositorio`() {
        val repo = repository
        detail(100, repo)
        detail(100, repo)
        assertEquals(1, api.collectorDetailCalls)
        assertEquals(1, api.collectorAlbumsCalls)
    }
    @Test fun `id inexistente muestra error sin seleccionar otro coleccionista`() {
        assertEquals(UiState.Error(R.string.error_collector_not_found), detail(999).state.value)
    }
    @Test fun `detalle permite reintentar una falla de red`() {
        val ok = api.collector
        api.collector = { throw IOException() }
        val vm = detail(100)
        assertEquals(UiState.Error(R.string.error_no_connection), vm.state.value)
        api.collector = ok
        vm.load(true)
        assertEquals(100, vm.data().id)
    }
    @Test fun `un error del servidor muestra el mensaje generico de servidor`() {
        api.collectorAlbums = { throw HttpException(Response.error<Any>(500, "".toResponseBody(null))) }
        assertEquals(UiState.Error(R.string.error_server), detail(100).state.value)
    }
    @Test fun `refreshFromCache muestra el album recien agregado sin otra llamada a la red`() = runTest {
        val repo = repository
        val vm = detail(7, repo)
        assertEquals(emptyList<String>(), vm.data().albums.map { it.album.name })
        repo.addAlbum(7, 100, NewCollectorAlbum(85000.0, CollectorAlbumStatus.ACTIVE))
        vm.refreshFromCache()
        assertEquals(listOf("Buscando América"), vm.data().albums.map { it.album.name })
        assertEquals(1, api.collectorDetailCalls)
    }
}
