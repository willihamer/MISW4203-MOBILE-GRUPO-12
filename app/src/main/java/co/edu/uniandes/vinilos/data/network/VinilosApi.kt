package co.edu.uniandes.vinilos.data.network

import co.edu.uniandes.vinilos.data.network.dto.AlbumDto
import co.edu.uniandes.vinilos.data.network.dto.BandDto
import co.edu.uniandes.vinilos.data.network.dto.CollectorDto
import co.edu.uniandes.vinilos.data.network.dto.MusicianDto
import retrofit2.http.GET
import retrofit2.http.Path

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

    @GET("musicians/{id}")
    suspend fun getMusician(@Path("id") id: Int): MusicianDto

    @GET("bands/{id}")
    suspend fun getBand(@Path("id") id: Int): BandDto
}
