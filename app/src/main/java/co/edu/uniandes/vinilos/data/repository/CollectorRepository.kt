package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CollectorRepository(
    private val network: NetworkServiceAdapter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    @Volatile
    private var collectorsCache: List<Collector>? = null

    suspend fun getCollectors(forceRefresh: Boolean = false): List<Collector> = withContext(dispatcher) {
        val cached = collectorsCache
        if (cached != null && !forceRefresh) cached else network.getCollectors().also { collectorsCache = it }
    }
}
