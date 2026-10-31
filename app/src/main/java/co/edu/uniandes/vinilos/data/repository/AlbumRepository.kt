package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Fuente única de álbumes para los ViewModels. El catálogo se guarda en memoria; el
 * botón de reintentar fuerza la red con [forceRefresh].
 */
class AlbumRepository(
    private val network: NetworkServiceAdapter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val detailCache = java.util.concurrent.ConcurrentHashMap<Int, Album>()

    suspend fun getAlbum(id: Int, forceRefresh: Boolean = false): Album = withContext(dispatcher) {
        val cached = detailCache[id]
        if (cached != null && !forceRefresh) cached
        else network.getAlbum(id).also { detailCache[id] = it }
    }

    @Volatile
    private var albumsCache: List<Album>? = null

    suspend fun getAlbums(forceRefresh: Boolean = false): List<Album> = withContext(dispatcher) {
        val cached = albumsCache
        if (cached != null && !forceRefresh) cached else network.getAlbums().also { albumsCache = it }
    }
}
