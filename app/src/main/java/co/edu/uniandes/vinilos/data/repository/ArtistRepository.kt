package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

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

    /** Detalles ya consultados, por tipo e id: músicos y bandas tienen ids independientes. */
    private val detailCache = ConcurrentHashMap<Pair<ArtistType, Int>, Artist>()

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

    /**
     * Detalle de un músico o una banda con sus álbumes y, si es banda, sus integrantes (HU04).
     * El tipo decide el endpoint porque el backend no tiene uno único de artistas.
     */
    suspend fun getArtist(id: Int, type: ArtistType, forceRefresh: Boolean = false): Artist =
        withContext(dispatcher) {
            val key = type to id
            val cached = detailCache[key]
            if (cached != null && !forceRefresh) {
                cached
            } else {
                val artist = when (type) {
                    ArtistType.MUSICIAN -> network.getMusician(id)
                    ArtistType.BAND -> network.getBand(id)
                }
                artist.also { detailCache[key] = it }
            }
        }
}
