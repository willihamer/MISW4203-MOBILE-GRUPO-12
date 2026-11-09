package co.edu.uniandes.vinilos.ui.collector

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.data.network.dto.CollectorDto
import co.edu.uniandes.vinilos.data.repository.CollectorRepository
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.ui.collector.detail.CollectorDetailViewModel
import co.edu.uniandes.vinilos.ui.collector.list.CollectorListViewModel
import co.edu.uniandes.vinilos.ui.common.UiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
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
    private val repository get() = CollectorRepository(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher)
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
    @Test fun `listado y detalle comparten cache y seleccionan por id`() {
        val repo = repository
        val vm = CollectorListViewModel(repo, SavedStateHandle())
        vm.load()
        val detail = CollectorDetailViewModel(repo, SavedStateHandle(mapOf("collectorId" to 42)))
        val collector = (detail.state.value as UiState.Success<Collector>).data
        assertEquals(42, collector.id)
        assertEquals("Ana López", collector.name)
        assertEquals(1, calls)
    }
    @Test fun `detalle recupera el coleccionista sin cache previa`() {
        val detail = CollectorDetailViewModel(repository, SavedStateHandle(mapOf("collectorId" to 7)))
        assertEquals("José Pérez", (detail.state.value as UiState.Success<Collector>).data.name)
    }
    @Test fun `id inexistente muestra error sin seleccionar otro coleccionista`() {
        val detail = CollectorDetailViewModel(repository, SavedStateHandle(mapOf("collectorId" to 999)))
        assertEquals(UiState.Error(R.string.error_collector_not_found), detail.state.value)
    }
    @Test fun `detalle permite reintentar una falla de red`() {
        api.collectors = { throw IOException() }
        val detail = CollectorDetailViewModel(repository, SavedStateHandle(mapOf("collectorId" to 7)))
        assertEquals(UiState.Error(R.string.error_no_connection), detail.state.value)
        api.collectors = { records }
        detail.load(true)
        assertEquals(7, (detail.state.value as UiState.Success<Collector>).data.id)
    }
}
