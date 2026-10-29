package co.edu.uniandes.vinilos.ui.artist.detail

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.ArtistRepository
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.messageRes
import kotlinx.coroutines.launch

/**
 * HU04 - Consultar detalle de un artista. Carga el músico o la banda que llega en los
 * argumentos de navegación, con sus álbumes y, si es banda, sus integrantes.
 */
class ArtistDetailViewModel(
    private val repository: ArtistRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val artistId: Int = checkNotNull(savedStateHandle[ARG_ARTIST_ID]) { "Falta el argumento $ARG_ARTIST_ID" }
    private val artistType: ArtistType = ArtistType.valueOf(
        checkNotNull(savedStateHandle[ARG_ARTIST_TYPE]) { "Falta el argumento $ARG_ARTIST_TYPE" },
    )

    private val _state = MutableLiveData<UiState<Artist>>()
    val state: LiveData<UiState<Artist>> = _state

    init {
        load()
    }

    fun load(forceRefresh: Boolean = false) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = try {
                UiState.Success(repository.getArtist(artistId, artistType, forceRefresh))
            } catch (e: AppError) {
                UiState.Error(e.detailMessageRes())
            }
        }
    }

    /** El backend responde 404 si el artista ya no existe; ese caso tiene su propio mensaje. */
    private fun AppError.detailMessageRes(): Int =
        if (this is AppError.Server && code == HTTP_NOT_FOUND) R.string.error_artist_not_found else messageRes()

    companion object {
        const val ARG_ARTIST_ID = "artistId"
        const val ARG_ARTIST_TYPE = "artistType"
        private const val HTTP_NOT_FOUND = 404

        /** Argumentos de navegación hacia el detalle del [artist]. */
        fun args(artist: Artist): Bundle = bundleOf(
            ARG_ARTIST_ID to artist.id,
            ARG_ARTIST_TYPE to artist.type.name,
        )
    }
}
