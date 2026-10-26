package co.edu.uniandes.vinilos.ui.common

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.uniandes.vinilos.di.AppContainer
import co.edu.uniandes.vinilos.ui.album.list.AlbumListViewModel
import co.edu.uniandes.vinilos.ui.profile.CollectorPickerViewModel
import co.edu.uniandes.vinilos.ui.profile.ProfileViewModel

/** Crea los ViewModels con sus dependencias tomadas del [AppContainer]. */
class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AlbumListViewModel::class.java) ->
            AlbumListViewModel(container.albumRepository)
        modelClass.isAssignableFrom(CollectorPickerViewModel::class.java) ->
            CollectorPickerViewModel(container.collectorRepository)
        modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
            ProfileViewModel(container.profileRepository)
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    } as T
}

val Fragment.viewModelFactory: ViewModelProvider.Factory
    get() = ViewModelFactory(appContainer)
