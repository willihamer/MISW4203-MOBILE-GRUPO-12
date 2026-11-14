package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.ArtistType

/**
 * Contrato de acceso a artistas (músicos y bandas) para los ViewModels.
 */
interface ArtistRepository {
    suspend fun getArtists(forceRefresh: Boolean = false): List<Artist>

    suspend fun getArtist(id: Int, type: ArtistType, forceRefresh: Boolean = false): Artist
}
