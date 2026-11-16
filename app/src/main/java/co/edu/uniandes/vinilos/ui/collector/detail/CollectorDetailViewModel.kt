package co.edu.uniandes.vinilos.ui.collector.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.CollectorDetail
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.CollectorRepository
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.messageRes
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * HU06 - Consultar detalle de un coleccionista. Carga el contacto, los artistas favoritos y
 * los álbumes del coleccionista que llega en los argumentos de navegación.
 */
class CollectorDetailViewModel(
    private val repository: CollectorRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val collectorId: Int = checkNotNull(savedState[ARG_COLLECTOR_ID]) { "Falta el argumento $ARG_COLLECTOR_ID" }
    private val _state = MutableLiveData<UiState<CollectorDetail>>()
    val state: LiveData<UiState<CollectorDetail>> = _state
    private var loadJob: Job? = null

    init {
        load()
    }

    fun load(forceRefresh: Boolean = false) {
        // Un reintento cancela la carga anterior: la respuesta más lenta no pisa a la más reciente
        loadJob?.cancel()
        _state.value = UiState.Loading
        loadJob = viewModelScope.launch {
            _state.value = try {
                UiState.Success(repository.getCollector(collectorId, forceRefresh))
            } catch (error: AppError) {
                UiState.Error(error.detailMessageRes())
            }
        }
    }

    /** El backend responde 404 si el coleccionista no existe; ese caso tiene su propio mensaje. */
    private fun AppError.detailMessageRes(): Int =
        if (this is AppError.Server && code == HTTP_NOT_FOUND) R.string.error_collector_not_found else messageRes()

    companion object {
        const val ARG_COLLECTOR_ID = "collectorId"
        private const val HTTP_NOT_FOUND = 404
    }
}
