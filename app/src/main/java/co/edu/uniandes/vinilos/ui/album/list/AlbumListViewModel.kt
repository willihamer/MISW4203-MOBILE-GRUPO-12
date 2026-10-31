package co.edu.uniandes.vinilos.ui.album.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.Genre
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.AlbumRepository
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.messageRes
import co.edu.uniandes.vinilos.ui.common.normalizedForSearch
import kotlinx.coroutines.launch

/**
 * HU01 - Catálogo de álbumes. Carga los álbumes y aplica la búsqueda y el filtro por
 * género. [UiState.Empty] significa que el catálogo no tiene álbumes; si hay álbumes
 * pero ninguno coincide con el filtro, el estado es [UiState.Success] con lista vacía.
 */
class AlbumListViewModel(private val repository: AlbumRepository) : ViewModel() {

    private val _state = MutableLiveData<UiState<List<Album>>>()
    val state: LiveData<UiState<List<Album>>> = _state

    private val _selectedGenre = MutableLiveData<Genre?>(null)
    val selectedGenre: LiveData<Genre?> = _selectedGenre

    private var albums: List<Album> = emptyList()
    private var query: String = ""

    init {
        load()
    }

    fun load(forceRefresh: Boolean = false) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                albums = repository.getAlbums(forceRefresh)
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

    fun onGenreSelected(genre: Genre?) {
        if (genre == _selectedGenre.value) return
        _selectedGenre.value = genre
        publishIfLoaded()
    }

    private fun publishIfLoaded() {
        val current = _state.value
        if (current is UiState.Success || current is UiState.Empty) publish()
    }

    private fun publish() {
        _state.value = if (albums.isEmpty()) UiState.Empty else UiState.Success(filter(albums))
    }

    private fun filter(source: List<Album>): List<Album> {
        val genre = _selectedGenre.value
        return source.filter { album ->
            (genre == null || album.genre == genre) && (query.isEmpty() || album.matches(query))
        }
    }

    private fun Album.matches(normalizedQuery: String): Boolean =
        name.normalizedForSearch().contains(normalizedQuery) ||
            recordLabel.normalizedForSearch().contains(normalizedQuery) ||
            performers.any { it.name.normalizedForSearch().contains(normalizedQuery) }
}
