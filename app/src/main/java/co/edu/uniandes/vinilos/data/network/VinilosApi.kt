package co.edu.uniandes.vinilos.data.network

import co.edu.uniandes.vinilos.data.network.dto.AddCollectorAlbumRequestDto
import co.edu.uniandes.vinilos.data.network.dto.AlbumDto
import co.edu.uniandes.vinilos.data.network.dto.CommentDto
import co.edu.uniandes.vinilos.data.network.dto.CreateCommentRequestDto
import co.edu.uniandes.vinilos.data.network.dto.BandDto
import co.edu.uniandes.vinilos.data.network.dto.CollectorAlbumDto
import co.edu.uniandes.vinilos.data.network.dto.CollectorDto
import co.edu.uniandes.vinilos.data.network.dto.CreateAlbumRequestDto
import co.edu.uniandes.vinilos.data.network.dto.CreateTrackRequestDto
import co.edu.uniandes.vinilos.data.network.dto.MusicianDto
import co.edu.uniandes.vinilos.data.network.dto.PerformerDto
import co.edu.uniandes.vinilos.data.network.dto.TrackDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Endpoints de BackVynils que usa la app. */
interface VinilosApi {
    @POST("albums/{id}/comments")
    suspend fun addComment(@Path("id") albumId: Int, @Body comment: CreateCommentRequestDto): CommentDto

    @GET("albums")
    suspend fun getAlbums(): List<AlbumDto>

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") id: Int): AlbumDto

    @POST("albums")
    suspend fun createAlbum(@Body album: CreateAlbumRequestDto): AlbumDto

    @POST("albums/{id}/tracks")
    suspend fun addTrack(@Path("id") albumId: Int, @Body track: CreateTrackRequestDto): TrackDto

    @GET("collectors")
    suspend fun getCollectors(): List<CollectorDto>

    @GET("collectors/{id}")
    suspend fun getCollector(@Path("id") id: Int): CollectorDto

    @GET("collectors/{id}/albums")
    suspend fun getCollectorAlbums(@Path("id") id: Int): List<CollectorAlbumDto>

    /** Lista vacía si el álbum no está en la colección; con un elemento si ya está. */
    @GET("collectors/{collectorId}/albums/{albumId}")
    suspend fun getCollectorAlbum(
        @Path("collectorId") collectorId: Int,
        @Path("albumId") albumId: Int,
    ): List<CollectorAlbumDto>

    @POST("collectors/{collectorId}/albums/{albumId}")
    suspend fun addAlbumToCollector(
        @Path("collectorId") collectorId: Int,
        @Path("albumId") albumId: Int,
        @Body album: AddCollectorAlbumRequestDto,
    ): CollectorAlbumDto

    /** Músicos y bandas favoritos del coleccionista (HU10); el backend comparte los ids entre ambos tipos. */
    @GET("collectors/{collectorId}/performers")
    suspend fun getFavoritePerformers(@Path("collectorId") collectorId: Int): List<PerformerDto>

    /** Responde el artista asociado; la app solo necesita saber que la operación terminó bien. */
    @POST("collectors/{collectorId}/musicians/{musicianId}")
    suspend fun addFavoriteMusician(
        @Path("collectorId") collectorId: Int,
        @Path("musicianId") musicianId: Int,
    ): PerformerDto

    @POST("collectors/{collectorId}/bands/{bandId}")
    suspend fun addFavoriteBand(
        @Path("collectorId") collectorId: Int,
        @Path("bandId") bandId: Int,
    ): PerformerDto

    @GET("musicians")
    suspend fun getMusicians(): List<MusicianDto>

    @GET("bands")
    suspend fun getBands(): List<BandDto>

    @GET("musicians/{id}")
    suspend fun getMusician(@Path("id") id: Int): MusicianDto

    @GET("bands/{id}")
    suspend fun getBand(@Path("id") id: Int): BandDto
}
