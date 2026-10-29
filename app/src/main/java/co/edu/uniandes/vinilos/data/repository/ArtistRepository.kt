package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Fuente única de artistas (músicos y bandas combinados) para los ViewModels.
 * Los datos se guardan en memoria; el botón de reintentar fuerza la red con [forceRefresh].
 */
class ArtistRepository(
    private val network: NetworkServiceAdapter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    @Volatile
    private var artistsCache: List<Artist>? = null

    suspend fun getArtists(forceRefresh: Boolean = false): List<Artist> = withContext(dispatcher) {
        val cached = artistsCache
        if (cached != null && !forceRefresh) {
            cached
        } else {
            coroutineScope {
                val musiciansDeferred = async { network.getMusicians() }
                val bandsDeferred = async { network.getBands() }
                val musicians = musiciansDeferred.await()
                val bands = bandsDeferred.await()
                val combined = (musicians + bands).sortedBy { it.name.lowercase() }
                combined.also { artistsCache = it }
            }
        }
    }
}
