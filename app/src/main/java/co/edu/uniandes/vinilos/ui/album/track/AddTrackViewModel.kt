package co.edu.uniandes.vinilos.ui.album.track

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.NewTrack
import co.edu.uniandes.vinilos.data.model.Track
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.AlbumRepository
import co.edu.uniandes.vinilos.ui.common.messageRes
import kotlinx.coroutines.launch

/** Campos y errores de validación del formulario de nuevo track (HU08). */
data class AddTrackFormState(
    val name: String = "",
    val duration: String = "",
    @param:StringRes val nameError: Int? = null,
    @param:StringRes val durationError: Int? = null,
)

/** Estado del guardado de un track (`POST /albums/{id}/tracks`). */
sealed interface SaveTrackUiState {
    data object Idle : SaveTrackUiState
    data object Saving : SaveTrackUiState
    data class Success(val track: Track) : SaveTrackUiState
    data class Error(@param:StringRes val message: Int) : SaveTrackUiState
}

/**
 * HU08 - Asociar un track a un álbum. Nombre y duración son obligatorios y la duración
 * debe tener el formato mm:ss. Mientras se guarda se ignoran nuevos envíos, para no
 * crear el mismo track dos veces con un doble toque.
 */
class AddTrackViewModel(
    private val repository: AlbumRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val albumId: Int = checkNotNull(savedStateHandle[ARG_ALBUM_ID])

    private val _formState = MutableLiveData(AddTrackFormState())
    val formState: LiveData<AddTrackFormState> = _formState

    private val _saveState = MutableLiveData<SaveTrackUiState>(SaveTrackUiState.Idle)
    val saveState: LiveData<SaveTrackUiState> = _saveState

    fun onNameChanged(value: String) {
        val current = _formState.value ?: AddTrackFormState()
        if (current.name == value && current.nameError == null) return
        _formState.value = current.copy(name = value, nameError = null)
        clearSubmitErrorIfNeeded()
    }

    fun onDurationChanged(value: String) {
        val current = _formState.value ?: AddTrackFormState()
        if (current.duration == value && current.durationError == null) return
        _formState.value = current.copy(duration = value, durationError = null)
        clearSubmitErrorIfNeeded()
    }

    fun submit() {
        if (_saveState.value is SaveTrackUiState.Saving) return

        val current = _formState.value ?: AddTrackFormState()
        val name = current.name.trim()
        val duration = current.duration.trim()

        val nameError = if (name.isEmpty()) R.string.track_add_error_required else null
        val durationError = when {
            duration.isEmpty() -> R.string.track_add_error_required
            !isValidTrackDuration(duration) -> R.string.track_add_error_duration_format
            else -> null
        }
        _formState.value = current.copy(nameError = nameError, durationError = durationError)
        if (nameError != null || durationError != null) return

        _saveState.value = SaveTrackUiState.Saving
        viewModelScope.launch {
            _saveState.value = try {
                SaveTrackUiState.Success(repository.addTrack(albumId, NewTrack(name, duration)))
            } catch (e: AppError) {
                SaveTrackUiState.Error(e.messageRes())
            }
        }
    }

    private fun clearSubmitErrorIfNeeded() {
        if (_saveState.value is SaveTrackUiState.Error) {
            _saveState.value = SaveTrackUiState.Idle
        }
    }

    companion object { const val ARG_ALBUM_ID = "albumId" }
}

/** Minutos de uno o dos dígitos y segundos entre 00 y 59; una duración de cero no es válida. */
private val TRACK_DURATION = Regex("""^(\d{1,2}):([0-5]\d)$""")

internal fun isValidTrackDuration(value: String): Boolean {
    val match = TRACK_DURATION.matchEntire(value.trim()) ?: return false
    val (minutes, seconds) = match.destructured
    return minutes.toInt() > 0 || seconds.toInt() > 0
}
