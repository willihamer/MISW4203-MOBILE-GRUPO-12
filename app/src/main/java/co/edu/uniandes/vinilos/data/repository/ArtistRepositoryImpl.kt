package co.edu.uniandes.vinilos.data.repository

import androidx.collection.LruCache
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Fuente única de artistas (músicos y bandas combinados) para los ViewModels.
 * Los datos se guardan en memoria; el botón de reintentar fuerza la red con [forceRefresh].
 */
class ArtistRepositoryImpl(
    private val network: NetworkServiceAdapter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ArtistRepository {
    @Volatile
    private var artistsCache: List<Artist>? = null

    /**
     * Detalles ya consultados, por tipo e id: músicos y bandas tienen ids independientes.
     * LruCache limita cuántos se guardan en memoria y descarta los menos usados.
     */
    private val detailCache = LruCache<Pair<ArtistType, Int>, Artist>(MAX_CACHED_DETAILS)

    override suspend fun getArtists(forceRefresh: Boolean): List<Artist> = withContext(dispatcher) {
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
    override suspend fun getArtist(id: Int, type: ArtistType, forceRefresh: Boolean): Artist =
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
                artist.also { detailCache.put(key, it) }
            }
        }

    override suspend fun getAvailableMusicians(bandId: Int): List<Artist.Musician> = coroutineScope {
        // La banda y el listado de artistas se piden en paralelo; ambos pasan por la caché
        val band = async { getArtist(bandId, ArtistType.BAND) as Artist.Band }
        val artists = async { getArtists() }
        val members = band.await().musicians.mapTo(HashSet()) { it.id }
        artists.await().filterIsInstance<Artist.Musician>().filter { it.id !in members }
    }

    override suspend fun addMusiciansToBand(bandId: Int, musicianIds: List<Int>): Artist.Band =
        withContext(dispatcher) {
            try {
                // Una petición por músico, en paralelo: el tiempo total es el de la más lenta
                coroutineScope { musicianIds.map { id -> async { network.addMusicianToBand(bandId, id) } }.awaitAll() }
            } finally {
                // Aunque una petición falle, otras pudieron guardarse. Además, en el backend un músico
                // pertenece a una sola banda: agregarlo aquí lo saca de la anterior. Se descartan las bandas en caché.
                detailCache.snapshot().keys.filter { it.first == ArtistType.BAND }.forEach { detailCache.remove(it) }
            }
            getArtist(bandId, ArtistType.BAND, forceRefresh = true) as Artist.Band
        }

    private companion object {
        /** Suficiente para navegar entre bandas, integrantes y músicos sin crecer sin límite. */
        const val MAX_CACHED_DETAILS = 30
    }
}
