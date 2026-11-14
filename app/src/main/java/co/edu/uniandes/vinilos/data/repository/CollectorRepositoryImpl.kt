package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CollectorRepositoryImpl(
    private val network: NetworkServiceAdapter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CollectorRepository {
    @Volatile
    private var collectorsCache: List<Collector>? = null

    override suspend fun getCollectors(forceRefresh: Boolean): List<Collector> = withContext(dispatcher) {
        val cached = collectorsCache
        if (cached != null && !forceRefresh) cached else network.getCollectors().also { collectorsCache = it }
    }
}
