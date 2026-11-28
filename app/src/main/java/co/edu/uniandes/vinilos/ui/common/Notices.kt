package co.edu.uniandes.vinilos.ui.common

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import co.edu.uniandes.vinilos.MainActivity

/** Muestra un aviso de la app sin tapar la barra inferior (ver [MainActivity.showNotice]). */
fun Fragment.showNotice(message: CharSequence) = (requireActivity() as MainActivity).showNotice(message)

fun Fragment.showNotice(@StringRes message: Int) = showNotice(getText(message))
