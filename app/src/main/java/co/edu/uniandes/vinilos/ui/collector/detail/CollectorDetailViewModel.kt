package co.edu.uniandes.vinilos.ui.collector.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.CollectorRepository
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.messageRes
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Datos básicos del coleccionista seleccionado, reutilizados desde el listado. */
class CollectorDetailViewModel(
    private val repository: CollectorRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val collectorId: Int = checkNotNull(savedState[ARG_COLLECTOR_ID])
    private val _state = MutableLiveData<UiState<Collector>>()
    val state: LiveData<UiState<Collector>> = _state
    private var loadJob: Job? = null
    init { load() }

    fun load(forceRefresh: Boolean = false) {
        loadJob?.cancel()
        _state.value = UiState.Loading
        loadJob = viewModelScope.launch {
            _state.value = try {
                val collector = repository.getCollectors(forceRefresh).firstOrNull { it.id == collectorId }
                if (collector == null) UiState.Error(R.string.error_collector_not_found)
                else UiState.Success(collector)
            } catch (error: AppError) {
                UiState.Error(error.messageRes())
            }
        }
    }
    companion object { const val ARG_COLLECTOR_ID = "collectorId" }
}
