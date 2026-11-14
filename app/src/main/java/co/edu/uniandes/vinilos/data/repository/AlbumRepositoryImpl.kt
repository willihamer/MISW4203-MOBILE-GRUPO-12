package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.NewAlbum
import co.edu.uniandes.vinilos.data.model.NewTrack
import co.edu.uniandes.vinilos.data.model.Track
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Fuente única de álbumes para los ViewModels. El catálogo se guarda en memoria; el
 * botón de reintentar fuerza la red con [forceRefresh]. Crear un álbum invalida la caché
 * del catálogo para que la siguiente consulta incluya el nuevo registro. Agregar un track
 * actualiza en memoria el detalle ya consultado, sin volver a pedir el álbum a la red.
 */
class AlbumRepositoryImpl(
    private val network: NetworkServiceAdapter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AlbumRepository {
    private val detailCache = ConcurrentHashMap<Int, Album>()

    @Volatile
    private var albumsCache: List<Album>? = null

    override val isCatalogCacheValid: Boolean
        get() = albumsCache != null

    override suspend fun getAlbum(id: Int, forceRefresh: Boolean): Album = withContext(dispatcher) {
        val cached = detailCache[id]
        if (cached != null && !forceRefresh) cached
        else network.getAlbum(id).also { detailCache[id] = it }
    }

    override suspend fun getAlbums(forceRefresh: Boolean): List<Album> = withContext(dispatcher) {
        val cached = albumsCache
        if (cached != null && !forceRefresh) cached else network.getAlbums().also { albumsCache = it }
    }

    override suspend fun createAlbum(album: NewAlbum): Album = withContext(dispatcher) {
        network.createAlbum(album).also { created ->
            albumsCache = null
            if (created.id != 0) {
                detailCache[created.id] = created
            }
        }
    }

    override suspend fun addTrack(albumId: Int, track: NewTrack): Track = withContext(dispatcher) {
        network.addTrack(albumId, track).also { created ->
            // get/put en lugar de computeIfPresent, que no existe antes de API 24
            detailCache[albumId]?.let { album -> detailCache[albumId] = album.copy(tracks = album.tracks + created) }
        }
    }
}
