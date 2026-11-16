package co.edu.uniandes.vinilos.data.repository

import androidx.collection.LruCache
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.model.CollectorDetail
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

    private companion object {
        /** Suficiente para ir y volver entre varios coleccionistas sin crecer sin límite. */
        const val MAX_CACHED_DETAILS = 20
    }
}
