package co.edu.uniandes.vinilos.ui.album.comment

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.NewComment
import co.edu.uniandes.vinilos.data.model.Profile
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.AlbumRepository
import co.edu.uniandes.vinilos.data.repository.ProfileRepository
import co.edu.uniandes.vinilos.ui.common.messageRes
import kotlinx.coroutines.launch

data class AddCommentFormState(
    val description: String = "",
    val rating: Int = 0,
    @param:StringRes val descriptionError: Int? = null,
    @param:StringRes val ratingError: Int? = null,
)

sealed interface SaveCommentUiState {
    data object Idle : SaveCommentUiState
    data object Saving : SaveCommentUiState
    data object Success : SaveCommentUiState
    data class Error(@param:StringRes val message: Int) : SaveCommentUiState
}

class AddCommentViewModel(
    private val repository: AlbumRepository,
    private val profileRepository: ProfileRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val albumId: Int = checkNotNull(savedStateHandle[ARG_ALBUM_ID])
    private val _formState = MutableLiveData(AddCommentFormState(
        description = savedStateHandle[KEY_DESCRIPTION] ?: "",
        rating = savedStateHandle[KEY_RATING] ?: 0,
    ))
    val formState: LiveData<AddCommentFormState> = _formState
    private val _saveState = MutableLiveData<SaveCommentUiState>(SaveCommentUiState.Idle)
    val saveState: LiveData<SaveCommentUiState> = _saveState
    val profile: LiveData<Profile?> = profileRepository.profile

    fun onDescriptionChanged(value: String) {
        if (_saveState.value is SaveCommentUiState.Saving) return
        savedStateHandle[KEY_DESCRIPTION] = value
        _formState.value = checkNotNull(_formState.value).copy(description = value, descriptionError = null)
        clearError()
    }

    fun onRatingChanged(value: Int) {
        if (_saveState.value is SaveCommentUiState.Saving) return
        savedStateHandle[KEY_RATING] = value
        _formState.value = checkNotNull(_formState.value).copy(rating = value, ratingError = null)
        clearError()
    }

    fun submit() {
        if (_saveState.value is SaveCommentUiState.Saving || _saveState.value is SaveCommentUiState.Success) return
        val collector = profileRepository.current as? Profile.CollectorProfile
        if (collector == null || collector.collectorId <= 0) {
            _saveState.value = SaveCommentUiState.Error(R.string.comment_collector_required)
            return
        }
        val form = checkNotNull(_formState.value)
        val description = form.description.trim()
        val descriptionError = if (description.isEmpty()) R.string.error_required_field else null
        val ratingError = if (form.rating !in 1..5) R.string.comment_rating_required else null
        _formState.value = form.copy(descriptionError = descriptionError, ratingError = ratingError)
        if (descriptionError != null || ratingError != null) return
        _saveState.value = SaveCommentUiState.Saving
        viewModelScope.launch {
            _saveState.value = try {
                repository.addComment(albumId, NewComment(description, form.rating, collector.collectorId))
                savedStateHandle[KEY_DESCRIPTION] = ""
                savedStateHandle[KEY_RATING] = 0
                SaveCommentUiState.Success
            } catch (e: AppError) {
                SaveCommentUiState.Error(e.messageRes())
            }
        }
    }

    private fun clearError() {
        if (_saveState.value is SaveCommentUiState.Error) _saveState.value = SaveCommentUiState.Idle
    }

    companion object {
        const val ARG_ALBUM_ID = "albumId"
        private const val KEY_DESCRIPTION = "commentDescription"
        private const val KEY_RATING = "commentRating"
    }
}
