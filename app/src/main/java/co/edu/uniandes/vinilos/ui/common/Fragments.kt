package co.edu.uniandes.vinilos.ui.common

import androidx.fragment.app.Fragment
import co.edu.uniandes.vinilos.VinilosApplication
import co.edu.uniandes.vinilos.di.AppContainer

val Fragment.appContainer: AppContainer
    get() = (requireActivity().application as VinilosApplication).container
