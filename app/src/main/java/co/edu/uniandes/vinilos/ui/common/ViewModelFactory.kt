package co.edu.uniandes.vinilos.ui.common

import co.edu.uniandes.vinilos.ui.album.detail.AlbumDetailViewModel
import co.edu.uniandes.vinilos.ui.collector.list.CollectorListViewModel
import co.edu.uniandes.vinilos.ui.collector.detail.CollectorDetailViewModel
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import co.edu.uniandes.vinilos.di.AppContainer
import co.edu.uniandes.vinilos.ui.album.list.AlbumListViewModel
import co.edu.uniandes.vinilos.ui.artist.detail.ArtistDetailViewModel
import co.edu.uniandes.vinilos.ui.artist.list.ArtistListViewModel
import co.edu.uniandes.vinilos.ui.profile.CollectorPickerViewModel
import co.edu.uniandes.vinilos.ui.profile.ProfileViewModel

/** Crea los ViewModels con sus dependencias tomadas del [AppContainer]. */
class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AlbumListViewModel::class.java) ->
            AlbumListViewModel(container.albumRepository)
        modelClass.isAssignableFrom(ArtistListViewModel::class.java) ->
            ArtistListViewModel(container.artistRepository)
        modelClass.isAssignableFrom(CollectorPickerViewModel::class.java) ->
            CollectorPickerViewModel(container.collectorRepository)
        modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
            ProfileViewModel(container.profileRepository)
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    } as T

    /** Los ViewModels de detalle reciben los argumentos de navegación en un [SavedStateHandle]. */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T = when {
        modelClass.isAssignableFrom(CollectorListViewModel::class.java) ->
            CollectorListViewModel(container.collectorRepository, extras.createSavedStateHandle()) as T
        modelClass.isAssignableFrom(CollectorDetailViewModel::class.java) ->
            CollectorDetailViewModel(container.collectorRepository, extras.createSavedStateHandle()) as T
        modelClass.isAssignableFrom(AlbumDetailViewModel::class.java) ->
            AlbumDetailViewModel(container.albumRepository, extras.createSavedStateHandle()) as T
        modelClass.isAssignableFrom(ArtistDetailViewModel::class.java) ->
            ArtistDetailViewModel(container.artistRepository, extras.createSavedStateHandle()) as T
        else -> create(modelClass)
    }
}

val Fragment.viewModelFactory: ViewModelProvider.Factory
    get() = ViewModelFactory(appContainer)
