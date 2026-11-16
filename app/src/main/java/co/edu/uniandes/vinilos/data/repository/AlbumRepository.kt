package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.NewAlbum
import co.edu.uniandes.vinilos.data.model.NewTrack
import co.edu.uniandes.vinilos.data.model.Track

/**
 * Contrato de acceso a álbumes para los ViewModels.
 */
interface AlbumRepository {
    val isCatalogCacheValid: Boolean

    suspend fun getAlbum(id: Int, forceRefresh: Boolean = false): Album

    suspend fun getAlbums(forceRefresh: Boolean = false): List<Album>

    suspend fun createAlbum(album: NewAlbum): Album

    suspend fun addTrack(albumId: Int, track: NewTrack): Track
}
