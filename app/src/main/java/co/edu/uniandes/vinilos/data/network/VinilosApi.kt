package co.edu.uniandes.vinilos.data.network

import co.edu.uniandes.vinilos.data.network.dto.AlbumDto
import co.edu.uniandes.vinilos.data.network.dto.BandDto
import co.edu.uniandes.vinilos.data.network.dto.CollectorDto
import co.edu.uniandes.vinilos.data.network.dto.MusicianDto
import retrofit2.http.GET

/** Endpoints de BackVynils que usa la app. */
interface VinilosApi {
    @GET("albums")
    suspend fun getAlbums(): List<AlbumDto>

    @GET("collectors")
    suspend fun getCollectors(): List<CollectorDto>

    @GET("musicians")
    suspend fun getMusicians(): List<MusicianDto>

    @GET("bands")
    suspend fun getBands(): List<BandDto>
}
