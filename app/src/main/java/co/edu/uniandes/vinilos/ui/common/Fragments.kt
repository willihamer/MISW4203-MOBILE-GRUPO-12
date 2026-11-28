package co.edu.uniandes.vinilos.ui.common

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.VinilosApplication
import co.edu.uniandes.vinilos.di.AppContainer

val Fragment.appContainer: AppContainer
    get() = (requireActivity().application as VinilosApplication).container

/**
 * Navega solo si la orden sigue teniendo sentido desde la pantalla actual. Con un doble toque rápido
 * los dos clics llegan antes de que la pantalla nueva tape la anterior (#136, encontrado con Monkey):
 * - Si `resId` es una acción que no existe desde el destino actual, NavController lanzaba
 *   IllegalArgumentException y la app se cerraba. Ahora el segundo toque se ignora.
 * - Si `resId` es un destino y ya estamos en él, el detalle se abría dos veces. También se ignora.
 */
fun Fragment.navigateSafely(@IdRes resId: Int, args: Bundle? = null, navOptions: NavOptions? = null) {
    val navController = findNavController()
    if (navController.canNavigate(resId)) navController.navigate(resId, args, navOptions)
}

internal fun NavController.canNavigate(@IdRes resId: Int): Boolean {
    val current = currentDestination ?: return false
    return when {
        current.getAction(resId) != null -> true
        graph.findNode(resId) != null -> current.id != resId
        else -> false
    }
}
