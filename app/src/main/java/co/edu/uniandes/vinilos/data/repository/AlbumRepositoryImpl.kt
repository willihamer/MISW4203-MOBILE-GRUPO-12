package co.edu.uniandes.vinilos.data.repository

import androidx.collection.LruCache
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.Comment
import co.edu.uniandes.vinilos.data.model.NewComment
import co.edu.uniandes.vinilos.data.model.NewAlbum
import co.edu.uniandes.vinilos.data.model.NewTrack
import co.edu.uniandes.vinilos.data.model.Track
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    /**
     * Detalles ya consultados por id. LruCache limita cuántos se guardan en memoria y descarta
     * los menos usados; es sincronizado, así que se puede usar desde varias corrutinas.
     */
    private val detailCache = LruCache<Int, Album>(MAX_CACHED_DETAILS)

    @Volatile
    private var albumsCache: List<Album>? = null

    override val isCatalogCacheValid: Boolean
        get() = albumsCache != null

    override suspend fun getAlbum(id: Int, forceRefresh: Boolean): Album = withContext(dispatcher) {
        val cached = detailCache[id]
        if (cached != null && !forceRefresh) cached
        else network.getAlbum(id).also { detailCache.put(id, it) }
    }

    override suspend fun getAlbums(forceRefresh: Boolean): List<Album> = withContext(dispatcher) {
        val cached = albumsCache
        if (cached != null && !forceRefresh) cached else network.getAlbums().also { albumsCache = it }
    }

    override suspend fun createAlbum(album: NewAlbum): Album = withContext(dispatcher) {
        network.createAlbum(album).also { created ->
            albumsCache = null
            if (created.id != 0) {
                detailCache.put(created.id, created)
            }
        }
    }

    override suspend fun addTrack(albumId: Int, track: NewTrack): Track = withContext(dispatcher) {
        network.addTrack(albumId, track).also { created ->
            detailCache[albumId]?.let { album -> detailCache.put(albumId, album.copy(tracks = album.tracks + created)) }
        }
    }

    private companion object {
        /** Suficiente para ir y volver entre varios álbumes sin crecer sin límite. */
        const val MAX_CACHED_DETAILS = 30
    }

    override suspend fun addComment(albumId: Int, comment: NewComment): Comment = withContext(dispatcher) {
        network.addComment(albumId, comment).also { created ->
            detailCache[albumId]?.let { album ->
                detailCache.put(albumId, album.copy(comments = album.comments + created))
            }
            albumsCache = null
        }
    }
}
