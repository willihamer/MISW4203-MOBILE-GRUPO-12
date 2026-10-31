package co.edu.uniandes.vinilos.ui.album.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.AlbumRepository
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.messageRes
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AlbumDetailViewModel(
    private val repository: AlbumRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val albumId: Int = checkNotNull(savedStateHandle[ARG_ALBUM_ID])
    private val _state = MutableLiveData<UiState<Album>>()
    val state: LiveData<UiState<Album>> = _state
    private var loadJob: Job? = null

    init { load() }

    fun load(forceRefresh: Boolean = false) {
        loadJob?.cancel()
        _state.value = UiState.Loading
        loadJob = viewModelScope.launch {
            _state.value = try {
                UiState.Success(repository.getAlbum(albumId, forceRefresh))
            } catch (e: AppError) {
                UiState.Error(if (e is AppError.Server && e.code == 404)
                    R.string.error_album_not_found else e.messageRes())
            }
        }
    }

    companion object { const val ARG_ALBUM_ID = "albumId" }
}
