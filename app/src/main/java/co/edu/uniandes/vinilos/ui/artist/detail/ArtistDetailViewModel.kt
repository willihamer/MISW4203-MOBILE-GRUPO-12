package co.edu.uniandes.vinilos.ui.artist.detail

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.data.model.Profile
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.ArtistRepository
import co.edu.uniandes.vinilos.data.repository.CollectorRepository
import co.edu.uniandes.vinilos.data.repository.ProfileRepository
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.messageRes
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Estado del botón de favorito en el detalle del artista (HU10). */
sealed interface FavoriteUiState {
    /** Modo visitante: la opción no se muestra. */
    data object Hidden : FavoriteUiState

    /** Se consulta si el artista ya es favorito del coleccionista. */
    data object Checking : FavoriteUiState

    data object NotFavorite : FavoriteUiState

    /** POST en curso: el botón no acepta otro toque. */
    data object Saving : FavoriteUiState

    data object Favorite : FavoriteUiState
}

/**
 * HU04 - Consultar detalle de un artista. Carga el músico o la banda que llega en los
 * argumentos de navegación, con sus álbumes y, si es banda, sus integrantes.
 *
 * HU10 - Agregar artistas favoritos. En modo coleccionista indica si el artista ya es favorito
 * y permite agregarlo una sola vez.
 */
class ArtistDetailViewModel(
    private val repository: ArtistRepository,
    private val collectorRepository: CollectorRepository,
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val artistId: Int = checkNotNull(savedStateHandle[ARG_ARTIST_ID]) { "Falta el argumento $ARG_ARTIST_ID" }
    private val artistType: ArtistType = ArtistType.valueOf(
        checkNotNull(savedStateHandle[ARG_ARTIST_TYPE]) { "Falta el argumento $ARG_ARTIST_TYPE" },
    )

    private val _state = MutableLiveData<UiState<Artist>>()
    val state: LiveData<UiState<Artist>> = _state
    private var loadJob: Job? = null

    private val _favoriteState = MutableLiveData<FavoriteUiState>(FavoriteUiState.Hidden)
    val favoriteState: LiveData<FavoriteUiState> = _favoriteState

    /** Mensaje para mostrar una vez; la vista llama a [onFavoriteMessageShown] después de mostrarlo. */
    private val _favoriteMessage = MutableLiveData<Int?>()
    val favoriteMessage: LiveData<Int?> = _favoriteMessage
    private var favoriteJob: Job? = null

    init {
        load()
    }

    fun load(forceRefresh: Boolean = false) {
        // Un reintento cancela la carga anterior: la respuesta más lenta no pisa a la más reciente
        loadJob?.cancel()
        _state.value = UiState.Loading
        loadJob = viewModelScope.launch {
            _state.value = try {
                UiState.Success(repository.getArtist(artistId, artistType, forceRefresh))
            } catch (e: AppError) {
                UiState.Error(e.detailMessageRes())
            }
        }
        checkFavorite()
    }

    /**
     * Muestra el artista que ya tiene el repositorio, sin pasar por el estado de carga; p. ej.
     * después de agregar integrantes a la banda (HU12), cuando el repositorio ya guardó la banda actualizada.
     */
    fun refreshFromCache() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.getArtist(artistId, artistType))
            } catch (_: AppError) {
                // Se conserva lo que ya se muestra
            }
        }
    }

    /** Agrega el artista a los favoritos del coleccionista activo; si ya lo es, solo lo indica. */
    fun onFavoriteClicked() {
        val collectorId = activeCollectorId() ?: return
        val artist = (_state.value as? UiState.Success)?.data ?: return
        when (_favoriteState.value) {
            FavoriteUiState.Favorite -> {
                _favoriteMessage.value = R.string.favorite_already_added
                return
            }
            FavoriteUiState.NotFavorite -> Unit
            // Oculto, consultando o guardando: un toque más no debe enviar otro POST
            else -> return
        }

        _favoriteState.value = FavoriteUiState.Saving
        favoriteJob = viewModelScope.launch {
            try {
                collectorRepository.addFavoriteArtist(collectorId, artist)
                _favoriteState.value = FavoriteUiState.Favorite
                _favoriteMessage.value = R.string.favorite_added
            } catch (_: AppError.AlreadyFavorite) {
                _favoriteState.value = FavoriteUiState.Favorite
                _favoriteMessage.value = R.string.favorite_already_added
            } catch (e: AppError) {
                _favoriteState.value = FavoriteUiState.NotFavorite
                _favoriteMessage.value = e.favoriteMessageRes()
            }
        }
    }

    fun onFavoriteMessageShown() {
        _favoriteMessage.value = null
    }

    private fun checkFavorite() {
        val collectorId = activeCollectorId()
        if (collectorId == null) {
            _favoriteState.value = FavoriteUiState.Hidden
            return
        }
        // Un reintento de la carga no interrumpe un POST en curso
        if (_favoriteState.value == FavoriteUiState.Saving) return

        favoriteJob?.cancel()
        _favoriteState.value = FavoriteUiState.Checking
        favoriteJob = viewModelScope.launch {
            _favoriteState.value = try {
                if (collectorRepository.isFavoriteArtist(collectorId, artistId)) {
                    FavoriteUiState.Favorite
                } else {
                    FavoriteUiState.NotFavorite
                }
            } catch (_: AppError) {
                // Si no se pudo consultar, se permite intentar: el repositorio verifica de nuevo antes del POST
                FavoriteUiState.NotFavorite
            }
        }
    }

    private fun activeCollectorId(): Int? =
        (profileRepository.current as? Profile.CollectorProfile)?.collectorId?.takeIf { it > 0 }

    /** El backend responde 404 si el artista ya no existe; ese caso tiene su propio mensaje. */
    private fun AppError.detailMessageRes(): Int =
        if (this is AppError.Server && code == HTTP_NOT_FOUND) R.string.error_artist_not_found else messageRes()

    /** Sin conexión se dice tal cual; cualquier otra falla, con un mensaje propio de la acción. */
    @StringRes
    private fun AppError.favoriteMessageRes(): Int =
        if (this is AppError.NoConnection) R.string.error_no_connection else R.string.favorite_error

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
