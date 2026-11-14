package co.edu.uniandes.vinilos.ui.common

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import co.edu.uniandes.vinilos.data.repository.AlbumRepository
import co.edu.uniandes.vinilos.data.repository.ArtistRepository
import co.edu.uniandes.vinilos.data.repository.CollectorRepository
import co.edu.uniandes.vinilos.data.repository.ProfileRepository
import co.edu.uniandes.vinilos.di.AppContainer
import co.edu.uniandes.vinilos.ui.album.create.CreateAlbumViewModel
import co.edu.uniandes.vinilos.ui.album.detail.AlbumDetailViewModel
import co.edu.uniandes.vinilos.ui.album.list.AlbumListViewModel
import co.edu.uniandes.vinilos.ui.album.track.AddTrackViewModel
import co.edu.uniandes.vinilos.ui.artist.detail.ArtistDetailViewModel
import co.edu.uniandes.vinilos.ui.artist.list.ArtistListViewModel
import co.edu.uniandes.vinilos.ui.collector.detail.CollectorDetailViewModel
import co.edu.uniandes.vinilos.ui.collector.list.CollectorListViewModel
import co.edu.uniandes.vinilos.ui.profile.CollectorPickerViewModel
import co.edu.uniandes.vinilos.ui.profile.ProfileViewModel

/** Crea los ViewModels con las interfaces de repositorio registradas en el [AppContainer]. */
class ViewModelFactory(
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val collectorRepository: CollectorRepository,
    private val profileRepository: ProfileRepository,
) : ViewModelProvider.Factory {

    constructor(container: AppContainer) : this(
        albumRepository = container.albumRepository,
        artistRepository = container.artistRepository,
        collectorRepository = container.collectorRepository,
        profileRepository = container.profileRepository,
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AlbumListViewModel::class.java) ->
            AlbumListViewModel(albumRepository)
        modelClass.isAssignableFrom(CreateAlbumViewModel::class.java) ->
            CreateAlbumViewModel(albumRepository)
        modelClass.isAssignableFrom(ArtistListViewModel::class.java) ->
            ArtistListViewModel(artistRepository)
        modelClass.isAssignableFrom(CollectorPickerViewModel::class.java) ->
            CollectorPickerViewModel(collectorRepository)
        modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
            ProfileViewModel(profileRepository)
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    } as T

    /** Los ViewModels de detalle reciben los argumentos de navegación en un [SavedStateHandle]. */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T = when {
        modelClass.isAssignableFrom(CollectorListViewModel::class.java) ->
            CollectorListViewModel(collectorRepository, extras.createSavedStateHandle()) as T
        modelClass.isAssignableFrom(CollectorDetailViewModel::class.java) ->
            CollectorDetailViewModel(collectorRepository, extras.createSavedStateHandle()) as T
        modelClass.isAssignableFrom(AlbumDetailViewModel::class.java) ->
            AlbumDetailViewModel(albumRepository, extras.createSavedStateHandle()) as T
        modelClass.isAssignableFrom(AddTrackViewModel::class.java) ->
            AddTrackViewModel(albumRepository, extras.createSavedStateHandle()) as T
        modelClass.isAssignableFrom(ArtistDetailViewModel::class.java) ->
            ArtistDetailViewModel(artistRepository, extras.createSavedStateHandle()) as T
        else -> create(modelClass)
    }
}

val Fragment.viewModelFactory: ViewModelProvider.Factory
    get() = ViewModelFactory(appContainer)
