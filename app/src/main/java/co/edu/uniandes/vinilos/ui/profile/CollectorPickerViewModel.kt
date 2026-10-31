package co.edu.uniandes.vinilos.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.CollectorRepository
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.messageRes
import kotlinx.coroutines.launch

/** P01b "¿Quién eres?": lista de coleccionistas para escoger el perfil. */
class CollectorPickerViewModel(private val repository: CollectorRepository) : ViewModel() {

    private val _state = MutableLiveData<UiState<List<Collector>>>()
    val state: LiveData<UiState<List<Collector>>> = _state

    private val _selected = MutableLiveData<Collector?>(null)
    val selected: LiveData<Collector?> = _selected

    init {
        load()
    }

    fun load(forceRefresh: Boolean = false) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = try {
                val collectors = repository.getCollectors(forceRefresh)
                if (collectors.isEmpty()) UiState.Empty else UiState.Success(collectors)
            } catch (e: AppError) {
                UiState.Error(e.messageRes())
            }
        }
    }

    fun select(collector: Collector) {
        _selected.value = collector
    }
}
