package co.edu.uniandes.vinilos.ui.album.collection

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.CollectorAlbumStatus
import co.edu.uniandes.vinilos.data.model.NewCollectorAlbum
import co.edu.uniandes.vinilos.data.model.Profile
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.repository.CollectorRepository
import co.edu.uniandes.vinilos.data.repository.ProfileRepository
import co.edu.uniandes.vinilos.ui.common.messageRes
import kotlinx.coroutines.launch

/** Campos y errores de validación del formulario para agregar un álbum a la colección (HU11). */
data class AddCollectorAlbumFormState(
    val price: String = "",
    val status: CollectorAlbumStatus? = null,
    @param:StringRes val priceError: Int? = null,
    @param:StringRes val statusError: Int? = null,
)

/** Estado del envío de `POST /collectors/{id}/albums/{id}` (HU11). */
sealed interface SaveCollectorAlbumUiState {
    data object Idle : SaveCollectorAlbumUiState
    data object Saving : SaveCollectorAlbumUiState
    data object Success : SaveCollectorAlbumUiState
    data object AlreadyInCollection : SaveCollectorAlbumUiState
    data class Error(@param:StringRes val message: Int) : SaveCollectorAlbumUiState
}

/**
 * HU11 - Agregar álbum a coleccionista. Precio y estado son obligatorios y el precio debe
 * ser mayor que cero. Conserva el borrador en [SavedStateHandle] ante recreación de la vista.
 */
class AddCollectorAlbumViewModel(
    private val repository: CollectorRepository,
    private val profileRepository: ProfileRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val albumId: Int = checkNotNull(savedStateHandle[ARG_ALBUM_ID])
    private val _formState = MutableLiveData(
        AddCollectorAlbumFormState(
            price = savedStateHandle[KEY_PRICE] ?: "",
            status = savedStateHandle.get<String>(KEY_STATUS)?.let(::parseStatus),
        ),
    )
    val formState: LiveData<AddCollectorAlbumFormState> = _formState

    private val _saveState = MutableLiveData<SaveCollectorAlbumUiState>(SaveCollectorAlbumUiState.Idle)
    val saveState: LiveData<SaveCollectorAlbumUiState> = _saveState

    fun onPriceChanged(value: String) {
        if (_saveState.value is SaveCollectorAlbumUiState.Saving) return
        savedStateHandle[KEY_PRICE] = value
        _formState.value = checkNotNull(_formState.value).copy(price = value, priceError = null)
        clearError()
    }

    fun onStatusChanged(value: CollectorAlbumStatus?) {
        if (_saveState.value is SaveCollectorAlbumUiState.Saving) return
        savedStateHandle[KEY_STATUS] = value?.name
        _formState.value = checkNotNull(_formState.value).copy(status = value, statusError = null)
        clearError()
    }

    fun submit() {
        val currentSaveState = _saveState.value
        if (
            currentSaveState is SaveCollectorAlbumUiState.Saving ||
            currentSaveState is SaveCollectorAlbumUiState.Success ||
            currentSaveState is SaveCollectorAlbumUiState.AlreadyInCollection
        ) {
            return
        }
        val collector = profileRepository.current as? Profile.CollectorProfile
        if (collector == null || collector.collectorId <= 0) {
            _saveState.value = SaveCollectorAlbumUiState.Error(R.string.collection_collector_required)
            return
        }

        val form = checkNotNull(_formState.value)
        val rawPrice = form.price.trim()
        val parsedPrice = parsePositivePrice(rawPrice)
        val priceError = when {
            rawPrice.isEmpty() -> R.string.error_required_field
            parsedPrice == null -> R.string.collection_price_invalid
            else -> null
        }
        val statusError = if (form.status == null) R.string.collection_status_required else null
        _formState.value = form.copy(priceError = priceError, statusError = statusError)
        if (priceError != null || statusError != null) return

        val validPrice = checkNotNull(parsedPrice)
        val validStatus = checkNotNull(form.status)
        _saveState.value = SaveCollectorAlbumUiState.Saving
        viewModelScope.launch {
            _saveState.value = try {
                repository.addAlbum(
                    collectorId = collector.collectorId,
                    albumId = albumId,
                    album = NewCollectorAlbum(price = validPrice, status = validStatus),
                )
                savedStateHandle[KEY_PRICE] = ""
                savedStateHandle[KEY_STATUS] = null
                SaveCollectorAlbumUiState.Success
            } catch (_: AppError.AlreadyInCollection) {
                SaveCollectorAlbumUiState.AlreadyInCollection
            } catch (e: AppError) {
                SaveCollectorAlbumUiState.Error(e.messageRes())
            }
        }
    }

    private fun clearError() {
        if (_saveState.value is SaveCollectorAlbumUiState.Error) {
            _saveState.value = SaveCollectorAlbumUiState.Idle
        }
    }

    companion object {
        const val ARG_ALBUM_ID = "albumId"
        private const val KEY_PRICE = "collectionPrice"
        private const val KEY_STATUS = "collectionStatus"
    }
}

private val PRICE_PATTERN = Regex("""^\d+([.,]\d+)?$""")

internal fun parsePositivePrice(value: String): Double? {
    val trimmed = value.trim()
    if (!PRICE_PATTERN.matches(trimmed)) return null
    val number = trimmed.replace(',', '.').toDoubleOrNull() ?: return null
    return number.takeIf { it.isFinite() && it > 0.0 }
}

private fun parseStatus(name: String): CollectorAlbumStatus? =
    CollectorAlbumStatus.entries.firstOrNull { it.name == name }
