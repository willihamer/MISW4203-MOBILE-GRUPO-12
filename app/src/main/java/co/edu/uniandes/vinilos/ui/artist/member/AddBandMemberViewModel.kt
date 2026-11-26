package co.edu.uniandes.vinilos.ui.artist.member

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.ArtistRepository
import co.edu.uniandes.vinilos.ui.common.Searchable
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.messageRes
import co.edu.uniandes.vinilos.ui.common.normalizedForSearch
import co.edu.uniandes.vinilos.ui.common.withSearchKeys
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Estado del guardado de los integrantes nuevos (`POST /bands/{id}/musicians/{id}`). */
sealed interface SaveMembersUiState {
    data object Idle : SaveMembersUiState
    data object Saving : SaveMembersUiState
    data class Success(val band: Artist.Band, val added: Int) : SaveMembersUiState
    data class Error(@param:StringRes val message: Int) : SaveMembersUiState
}

/**
 * HU12 - Agregar músicos a una banda (P12). Lista los músicos que no pertenecen a la banda,
 * permite buscarlos y escoger uno o varios. [UiState.Empty] significa que todos los músicos ya
 * son integrantes; si la búsqueda no coincide con ninguno, el estado es [UiState.Success] vacío.
 */
class AddBandMemberViewModel(
    private val repository: ArtistRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val bandId: Int = checkNotNull(savedStateHandle[ARG_BAND_ID]) { "Falta el argumento $ARG_BAND_ID" }

    private val _state = MutableLiveData<UiState<List<Artist.Musician>>>()
    val state: LiveData<UiState<List<Artist.Musician>>> = _state

    private val _selected = MutableLiveData<Set<Int>>(emptySet())
    val selected: LiveData<Set<Int>> = _selected

    private val _saveState = MutableLiveData<SaveMembersUiState>(SaveMembersUiState.Idle)
    val saveState: LiveData<SaveMembersUiState> = _saveState

    private var musicians: List<Searchable<Artist.Musician>> = emptyList()
    private var query: String = ""
    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        // Un reintento cancela la carga anterior: la respuesta más lenta no pisa a la más reciente
        loadJob?.cancel()
        _state.value = UiState.Loading
        loadJob = viewModelScope.launch {
            try {
                musicians = repository.getAvailableMusicians(bandId).withSearchKeys { listOf(it.name) }
                publish()
            } catch (e: AppError) {
                _state.value = UiState.Error(e.messageRes())
            }
        }
    }

    fun onQueryChanged(text: String) {
        val normalized = text.normalizedForSearch()
        if (normalized == query) return
        query = normalized
        val current = _state.value
        if (current is UiState.Success || current is UiState.Empty) publish()
    }

    /** Selecciona o quita de la selección al músico; la selección se conserva al buscar. */
    fun onMusicianClicked(musicianId: Int) {
        if (_saveState.value is SaveMembersUiState.Saving) return
        val current = _selected.value.orEmpty()
        _selected.value = if (musicianId in current) current - musicianId else current + musicianId
        if (_saveState.value is SaveMembersUiState.Error) _saveState.value = SaveMembersUiState.Idle
    }

    fun submit() {
        // Mientras se guarda, un segundo toque no envía las mismas peticiones otra vez
        if (_saveState.value is SaveMembersUiState.Saving) return
        // Se conserva el orden de la lista para que los integrantes se envíen como se ven
        val ids = musicians.map { it.item.id }.filter { it in _selected.value.orEmpty() }
        if (ids.isEmpty()) return

        _saveState.value = SaveMembersUiState.Saving
        viewModelScope.launch {
            _saveState.value = try {
                SaveMembersUiState.Success(repository.addMusiciansToBand(bandId, ids), ids.size)
            } catch (e: AppError) {
                SaveMembersUiState.Error(e.messageRes())
            }
        }
    }

    private fun publish() {
        _state.value = when {
            musicians.isEmpty() -> UiState.Empty
            query.isEmpty() -> UiState.Success(musicians.map { it.item })
            else -> UiState.Success(musicians.filter { it.key.contains(query) }.map { it.item })
        }
    }

    companion object { const val ARG_BAND_ID = "bandId" }
}
