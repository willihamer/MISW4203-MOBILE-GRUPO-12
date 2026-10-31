package co.edu.uniandes.vinilos.data.network

import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.network.mapper.toModel
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Service Adapter: único punto que conoce Retrofit. Ejecuta las llamadas, convierte los
 * DTO en modelos de la app y traduce cualquier falla a [AppError].
 */
class NetworkServiceAdapter(private val api: VinilosApi) {

    suspend fun getAlbums(): List<Album> = call { api.getAlbums().map { it.toModel() } }

    suspend fun getAlbum(id: Int): Album = call { api.getAlbum(id).toModel() }

    suspend fun getCollectors(): List<Collector> = call { api.getCollectors().map { it.toModel() } }

    suspend fun getMusicians(): List<Artist.Musician> = call { api.getMusicians().map { it.toModel() } }

    suspend fun getBands(): List<Artist.Band> = call { api.getBands().map { it.toModel() } }

    suspend fun getMusician(id: Int): Artist.Musician = call { api.getMusician(id).toModel() }

    suspend fun getBand(id: Int): Artist.Band = call { api.getBand(id).toModel() }

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
