package co.edu.uniandes.vinilos.ui.common

import androidx.annotation.StringRes

/** Estado de una pantalla que carga datos: cargando, contenido, vacío o error con reintento. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data object Empty : UiState<Nothing>
    data class Error(@param:StringRes val message: Int) : UiState<Nothing>
}
