package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.model.CollectorAlbum
import co.edu.uniandes.vinilos.data.model.CollectorDetail
import co.edu.uniandes.vinilos.data.model.NewCollectorAlbum

/**
 * Contrato de acceso a coleccionistas para los ViewModels.
 */
interface CollectorRepository {
    suspend fun getCollectors(forceRefresh: Boolean = false): List<Collector>

    /** Contacto, artistas favoritos y álbumes de la colección del coleccionista [id] (HU06). */
    suspend fun getCollector(id: Int, forceRefresh: Boolean = false): CollectorDetail

    /**
     * Agrega el álbum [albumId] a la colección de [collectorId] con su precio y estado (HU11).
     * Lanza [co.edu.uniandes.vinilos.data.network.AppError.AlreadyInCollection] si ya estaba.
     */
    suspend fun addAlbum(collectorId: Int, albumId: Int, album: NewCollectorAlbum): CollectorAlbum

    /** Indica si [artistId] ya está entre los artistas favoritos de [collectorId] (HU10). */
    suspend fun isFavoriteArtist(collectorId: Int, artistId: Int): Boolean

    /**
     * Agrega el músico o la banda [artist] a los favoritos de [collectorId] (HU10).
     * Lanza [co.edu.uniandes.vinilos.data.network.AppError.AlreadyFavorite] si ya estaba.
     */
    suspend fun addFavoriteArtist(collectorId: Int, artist: Artist)
}
