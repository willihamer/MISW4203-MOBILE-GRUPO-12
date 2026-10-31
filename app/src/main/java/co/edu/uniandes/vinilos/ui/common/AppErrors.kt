package co.edu.uniandes.vinilos.ui.common

import androidx.annotation.StringRes
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.network.AppError

@StringRes
fun AppError.messageRes(): Int = when (this) {
    is AppError.NoConnection -> R.string.error_no_connection
    is AppError.Server -> R.string.error_server
    is AppError.Unknown -> R.string.error_unknown
}
