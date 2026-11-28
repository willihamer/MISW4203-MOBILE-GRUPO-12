package co.edu.uniandes.vinilos.data.network

import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.Comment
import co.edu.uniandes.vinilos.data.model.NewComment
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.model.CollectorAlbum
import co.edu.uniandes.vinilos.data.model.CollectorDetail
import co.edu.uniandes.vinilos.data.model.NewAlbum
import co.edu.uniandes.vinilos.data.model.NewCollectorAlbum
import co.edu.uniandes.vinilos.data.model.NewTrack
import co.edu.uniandes.vinilos.data.model.Performer
import co.edu.uniandes.vinilos.data.model.Track
import co.edu.uniandes.vinilos.data.network.mapper.toDetail
import co.edu.uniandes.vinilos.data.network.mapper.toModel
import co.edu.uniandes.vinilos.data.network.mapper.toRequestDto
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Service Adapter: único punto que conoce Retrofit. Ejecuta las llamadas, convierte los
 * DTO en modelos de la app y traduce cualquier falla a [AppError].
 */
class NetworkServiceAdapter(private val api: VinilosApi) {

    suspend fun addComment(albumId: Int, comment: NewComment): Comment =
        call { api.addComment(albumId, comment.toRequestDto()).toModel() }

    suspend fun getAlbums(): List<Album> = call { api.getAlbums().map { it.toModel() } }

    suspend fun getAlbum(id: Int): Album = call { api.getAlbum(id).toModel() }

    suspend fun createAlbum(album: NewAlbum): Album = call { api.createAlbum(album.toRequestDto()).toModel() }

    suspend fun addTrack(albumId: Int, track: NewTrack): Track =
        call { api.addTrack(albumId, track.toRequestDto()).toModel() }

    suspend fun getCollectors(): List<Collector> = call { api.getCollectors().map { it.toModel() } }

    /** Contacto y artistas favoritos; los álbumes llegan aparte en [getCollectorAlbums]. */
    suspend fun getCollector(id: Int): CollectorDetail = call { api.getCollector(id).toDetail() }

    suspend fun getCollectorAlbums(id: Int): List<CollectorAlbum> =
        call { api.getCollectorAlbums(id).mapNotNull { it.toModel() } }

    /** GET /collectors/{id}/albums/{albumId}: el backend responde una lista vacía si no está. */
    suspend fun isAlbumInCollection(collectorId: Int, albumId: Int): Boolean =
        call { api.getCollectorAlbum(collectorId, albumId).isNotEmpty() }

    /** Una respuesta sin álbum no se puede mostrar en la colección: termina en [AppError.Unknown]. */
    suspend fun addAlbumToCollector(collectorId: Int, albumId: Int, album: NewCollectorAlbum): CollectorAlbum =
        call { checkNotNull(api.addAlbumToCollector(collectorId, albumId, album.toRequestDto()).toModel()) }

    /** GET /collectors/{id}/performers: músicos y bandas favoritos del coleccionista (HU10). */
    suspend fun getFavoritePerformers(collectorId: Int): List<Performer> =
        call { api.getFavoritePerformers(collectorId).map { it.toModel() } }

    /** Marca como favorito al músico o a la banda, cada uno con su endpoint (HU10). */
    suspend fun addFavoritePerformer(collectorId: Int, performerId: Int, type: ArtistType) {
        call {
            when (type) {
                ArtistType.MUSICIAN -> api.addFavoriteMusician(collectorId, performerId)
                ArtistType.BAND -> api.addFavoriteBand(collectorId, performerId)
            }
        }
    }

    suspend fun getMusicians(): List<Artist.Musician> = call { api.getMusicians().map { it.toModel() } }

    suspend fun getBands(): List<Artist.Band> = call { api.getBands().map { it.toModel() } }

    suspend fun getMusician(id: Int): Artist.Musician = call { api.getMusician(id).toModel() }

    suspend fun getBand(id: Int): Artist.Band = call { api.getBand(id).toModel() }

    /** POST /bands/{bandId}/musicians/{musicianId}: agrega el músico a la banda (HU12). */
    suspend fun addMusicianToBand(bandId: Int, musicianId: Int): Artist.Musician =
        call { api.addMusicianToBand(bandId, musicianId).toModel() }

    private suspend fun <T> call(block: suspend () -> T): T {
        NetworkIdlingResource.increment()
        return try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            throw AppError.NoConnection()
        } catch (e: HttpException) {
            throw AppError.Server(e.code())
        } catch (e: Exception) {
            throw AppError.Unknown()
        } finally {
            NetworkIdlingResource.decrement()
        }
    }
}
