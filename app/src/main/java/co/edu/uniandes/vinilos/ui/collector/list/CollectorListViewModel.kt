package co.edu.uniandes.vinilos.ui.collector.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.CollectorRepository
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.messageRes
import co.edu.uniandes.vinilos.ui.common.normalizedForSearch
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CollectorListViewModel(
    private val repository: CollectorRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableLiveData<UiState<List<Collector>>>()
    val state: LiveData<UiState<List<Collector>>> = _state
    val query: String get() = savedState[QUERY] ?: ""
    private var collectors: List<Collector> = emptyList()
    private var loadJob: Job? = null

    init { load() }

    fun load(forceRefresh: Boolean = false) {
        loadJob?.cancel()
        _state.value = UiState.Loading
        loadJob = viewModelScope.launch {
            try {
                collectors = repository.getCollectors(forceRefresh)
                publish()
            } catch (error: AppError) {
                _state.value = UiState.Error(error.messageRes())
            }
        }
    }

    fun onQueryChanged(text: String) {
        savedState[QUERY] = text
        if (_state.value is UiState.Success || _state.value is UiState.Empty) publish()
    }

    private fun publish() {
        val term = query.normalizedForSearch()
        _state.value = if (collectors.isEmpty()) UiState.Empty else UiState.Success(
            collectors.filter { it.name.normalizedForSearch().contains(term) },
        )
    }

    private companion object { const val QUERY = "collectorQuery" }
}
