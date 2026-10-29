package co.edu.uniandes.vinilos.ui.artist.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.ArtistRepository
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.messageRes
import co.edu.uniandes.vinilos.ui.common.normalizedForSearch
import kotlinx.coroutines.launch

/**
 * HU03 - Consultar listado de artistas. Carga músicos y bandas combinados y aplica
 * la búsqueda por nombre y el filtro por tipo de artista. [UiState.Empty] significa
 * que el catálogo no tiene artistas; si hay artistas pero ninguno coincide con el
 * filtro o la búsqueda, el estado es [UiState.Success] con lista vacía.
 */
class ArtistListViewModel(private val repository: ArtistRepository) : ViewModel() {

    private val _state = MutableLiveData<UiState<List<Artist>>>()
    val state: LiveData<UiState<List<Artist>>> = _state

    private val _selectedType = MutableLiveData<ArtistType?>(null)
    val selectedType: LiveData<ArtistType?> = _selectedType

    private var artists: List<Artist> = emptyList()
    private var query: String = ""

    init {
        load()
    }

    fun load(forceRefresh: Boolean = false) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                artists = repository.getArtists(forceRefresh)
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
        publishIfLoaded()
    }

    fun onTypeSelected(type: ArtistType?) {
        if (type == _selectedType.value) return
        _selectedType.value = type
        publishIfLoaded()
    }

    private fun publishIfLoaded() {
        val current = _state.value
        if (current is UiState.Success || current is UiState.Empty) publish()
    }

    private fun publish() {
        _state.value = if (artists.isEmpty()) UiState.Empty else UiState.Success(filter(artists))
    }

    private fun filter(source: List<Artist>): List<Artist> {
        val type = _selectedType.value
        return source.filter { artist ->
            (type == null || artist.type == type) && (query.isEmpty() || artist.matches(query))
        }
    }

    private fun Artist.matches(normalizedQuery: String): Boolean =
        name.normalizedForSearch().contains(normalizedQuery)
}
