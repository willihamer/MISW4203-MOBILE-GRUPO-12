package co.edu.uniandes.vinilos.data.repository

import androidx.collection.LruCache
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.model.CollectorAlbum
import co.edu.uniandes.vinilos.data.model.CollectorDetail
import co.edu.uniandes.vinilos.data.model.NewCollectorAlbum
import co.edu.uniandes.vinilos.data.model.Performer
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class CollectorRepositoryImpl(
    private val network: NetworkServiceAdapter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CollectorRepository {
    @Volatile
    private var collectorsCache: List<Collector>? = null

    /**
     * Detalles ya consultados por id. LruCache limita cuántos se guardan en memoria y descarta
     * los menos usados; es sincronizado, así que se puede usar desde varias corrutinas.
     */
    private val detailCache = LruCache<Int, CollectorDetail>(MAX_CACHED_DETAILS)

    override suspend fun getCollectors(forceRefresh: Boolean): List<Collector> = withContext(dispatcher) {
        val cached = collectorsCache
        if (cached != null && !forceRefresh) cached else network.getCollectors().also { collectorsCache = it }
    }

    /**
     * El backend entrega el contacto y los álbumes en dos endpoints. Se consultan en paralelo:
     * si uno falla, coroutineScope cancela el otro y propaga el error.
     */
    override suspend fun getCollector(id: Int, forceRefresh: Boolean): CollectorDetail = withContext(dispatcher) {
        val cached = detailCache[id]
        if (cached != null && !forceRefresh) {
            cached
        } else {
            coroutineScope {
                val collector = async { network.getCollector(id) }
                val albums = async { network.getCollectorAlbums(id) }
                collector.await().copy(albums = albums.await())
            }.also { detailCache.put(id, it) }
        }
    }

    /**
     * El backend no impide agregar dos veces el mismo álbum, así que se verifica antes del POST:
     * primero en el detalle en caché y, si no está ahí, en el servidor. Al guardar, el álbum se
     * anexa al detalle en caché para que la colección (HU06) lo muestre sin volver a la red.
     */
    override suspend fun addAlbum(collectorId: Int, albumId: Int, album: NewCollectorAlbum): CollectorAlbum =
        withContext(dispatcher) {
            val inCachedCollection = detailCache[collectorId]?.albums?.any { it.album.id == albumId } == true
            if (inCachedCollection || network.isAlbumInCollection(collectorId, albumId)) {
                throw AppError.AlreadyInCollection()
            }
            network.addAlbumToCollector(collectorId, albumId, album).also { created ->
                detailCache[collectorId]?.let { detail ->
                    detailCache.put(collectorId, detail.copy(albums = detail.albums + created))
                }
            }
        }

    /**
     * Si el detalle del coleccionista ya está en caché (HU06), responde sin red. Si no, consulta
     * solo sus favoritos, que es más liviano que traer el detalle con toda la colección.
     */
    override suspend fun isFavoriteArtist(collectorId: Int, artistId: Int): Boolean = withContext(dispatcher) {
        detailCache[collectorId]?.favoritePerformers?.any { it.id == artistId }
            ?: network.getFavoritePerformers(collectorId).any { it.id == artistId }
    }

    /**
     * El backend no impide marcar dos veces el mismo favorito, así que se verifica antes del POST:
     * primero en la caché y, si no está ahí, en el servidor. Al guardar, el artista se anexa al
     * detalle en caché para que los favoritos de HU06 lo muestren sin volver a la red.
     */
    override suspend fun addFavoriteArtist(collectorId: Int, artist: Artist) = withContext(dispatcher) {
        val performer = Performer(id = artist.id, name = artist.name, image = artist.image, type = artist.type)
        val inCachedFavorites = detailCache[collectorId]?.favoritePerformers?.any { it.id == artist.id } == true
        if (inCachedFavorites) throw AppError.AlreadyFavorite()
        if (network.getFavoritePerformers(collectorId).any { it.id == artist.id }) {
            // La caché estaba desactualizada: se corrige para que HU06 lo muestre
            appendFavoriteToCache(collectorId, performer)
            throw AppError.AlreadyFavorite()
        }
        network.addFavoritePerformer(collectorId, artist.id, artist.type)
        appendFavoriteToCache(collectorId, performer)
    }

    private fun appendFavoriteToCache(collectorId: Int, performer: Performer) {
        detailCache[collectorId]?.let { detail ->
            detailCache.put(collectorId, detail.copy(favoritePerformers = detail.favoritePerformers + performer))
        }
    }

    private companion object {
        /** Suficiente para ir y volver entre varios coleccionistas sin crecer sin límite. */
        const val MAX_CACHED_DETAILS = 20
    }
}
