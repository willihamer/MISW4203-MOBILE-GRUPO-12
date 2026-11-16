package co.edu.uniandes.vinilos.ui.album.create

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.Genre
import co.edu.uniandes.vinilos.data.model.NewAlbum
import co.edu.uniandes.vinilos.data.model.RecordLabel
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.AlbumRepository
import co.edu.uniandes.vinilos.ui.common.messageRes
import kotlinx.coroutines.launch
import java.net.URI
import java.time.LocalDate

/** Estado reactivo de los campos y errores de validación del formulario de creación (HU07). */
data class CreateAlbumFormState(
    val name: String = "",
    val coverUrl: String = "",
    val releaseDate: LocalDate? = null,
    val genre: Genre? = null,
    val recordLabel: RecordLabel? = null,
    val description: String = "",
    @param:StringRes val nameError: Int? = null,
    @param:StringRes val coverError: Int? = null,
    @param:StringRes val releaseDateError: Int? = null,
    @param:StringRes val genreError: Int? = null,
    @param:StringRes val recordLabelError: Int? = null,
    @param:StringRes val descriptionError: Int? = null,
) {
    val hasValidCoverPreview: Boolean
        get() = isValidHttpUrl(coverUrl)
}

/** Estado del proceso de guardado de un nuevo álbum (`POST /albums`). */
sealed interface SaveAlbumUiState {
    data object Idle : SaveAlbumUiState
    data object Saving : SaveAlbumUiState
    data class Success(val album: Album) : SaveAlbumUiState
    data class Error(@param:StringRes val message: Int) : SaveAlbumUiState
}

/**
 * HU07 - Crear un álbum. Valida que los seis campos sean obligatorios, que la URL de
 * portada tenga esquema HTTP/HTTPS y que el género y el sello pertenezcan a sus listas cerradas.
 */
class CreateAlbumViewModel(private val repository: AlbumRepository) : ViewModel() {

    private val _formState = MutableLiveData(CreateAlbumFormState())
    val formState: LiveData<CreateAlbumFormState> = _formState

    private val _saveState = MutableLiveData<SaveAlbumUiState>(SaveAlbumUiState.Idle)
    val saveState: LiveData<SaveAlbumUiState> = _saveState

    fun onNameChanged(value: String) {
        val current = _formState.value ?: CreateAlbumFormState()
        if (current.name == value && current.nameError == null) return
        _formState.value = current.copy(name = value, nameError = null)
        clearSubmitErrorIfNeeded()
    }

    fun onCoverUrlChanged(value: String) {
        val current = _formState.value ?: CreateAlbumFormState()
        if (current.coverUrl == value && current.coverError == null) return
        _formState.value = current.copy(coverUrl = value, coverError = null)
        clearSubmitErrorIfNeeded()
    }

    fun onReleaseDateSelected(date: LocalDate) {
        val current = _formState.value ?: CreateAlbumFormState()
        _formState.value = current.copy(releaseDate = date, releaseDateError = null)
        clearSubmitErrorIfNeeded()
    }

    fun onGenreSelected(genre: Genre) {
        val current = _formState.value ?: CreateAlbumFormState()
        _formState.value = current.copy(genre = genre, genreError = null)
        clearSubmitErrorIfNeeded()
    }

    fun onRecordLabelSelected(recordLabel: RecordLabel) {
        val current = _formState.value ?: CreateAlbumFormState()
        _formState.value = current.copy(recordLabel = recordLabel, recordLabelError = null)
        clearSubmitErrorIfNeeded()
    }

    fun onDescriptionChanged(value: String) {
        val current = _formState.value ?: CreateAlbumFormState()
        if (current.description == value && current.descriptionError == null) return
        _formState.value = current.copy(description = value, descriptionError = null)
        clearSubmitErrorIfNeeded()
    }

    fun submit() {
        if (_saveState.value is SaveAlbumUiState.Saving) return

        val current = _formState.value ?: CreateAlbumFormState()
        val trimmedName = current.name.trim()
        val trimmedCover = current.coverUrl.trim()
        val trimmedDescription = current.description.trim()

        val nameError = if (trimmedName.isEmpty()) R.string.error_required_field else null
        val coverError = when {
            trimmedCover.isEmpty() -> R.string.error_required_field
            !isValidHttpUrl(trimmedCover) -> R.string.album_create_error_cover_url
            else -> null
        }
        val releaseDateError = if (current.releaseDate == null) R.string.error_required_field else null
        val genreError = if (current.genre == null) R.string.error_required_field else null
        val recordLabelError = if (current.recordLabel == null) R.string.error_required_field else null
        val descriptionError = if (trimmedDescription.isEmpty()) R.string.error_required_field else null

        val validated = current.copy(
            nameError = nameError,
            coverError = coverError,
            releaseDateError = releaseDateError,
            genreError = genreError,
            recordLabelError = recordLabelError,
            descriptionError = descriptionError,
        )
        _formState.value = validated

        if (
            nameError != null ||
            coverError != null ||
            releaseDateError != null ||
            genreError != null ||
            recordLabelError != null ||
            descriptionError != null
        ) {
            return
        }

        val newAlbum = NewAlbum(
            name = trimmedName,
            cover = trimmedCover,
            releaseDate = checkNotNull(current.releaseDate),
            description = trimmedDescription,
            genre = checkNotNull(current.genre),
            recordLabel = checkNotNull(current.recordLabel),
        )

        _saveState.value = SaveAlbumUiState.Saving
        viewModelScope.launch {
            try {
                val created = repository.createAlbum(newAlbum)
                _saveState.value = SaveAlbumUiState.Success(created)
            } catch (e: AppError) {
                _saveState.value = SaveAlbumUiState.Error(e.messageRes())
            }
        }
    }

    private fun clearSubmitErrorIfNeeded() {
        if (_saveState.value is SaveAlbumUiState.Error) {
            _saveState.value = SaveAlbumUiState.Idle
        }
    }
}

internal fun isValidHttpUrl(value: String): Boolean {
    val trimmed = value.trim()
    if (trimmed.isEmpty() || trimmed.contains(' ')) return false
    return try {
        val uri = URI(trimmed)
        val scheme = uri.scheme?.lowercase()
        (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
    } catch (_: Exception) {
        false
    }
}
