package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.ArtistType

/**
 * Contrato de acceso a artistas (músicos y bandas) para los ViewModels.
 */
interface ArtistRepository {
    suspend fun getArtists(forceRefresh: Boolean = false): List<Artist>

    suspend fun getArtist(id: Int, type: ArtistType, forceRefresh: Boolean = false): Artist

    /** Músicos que se pueden agregar a la banda: todos los músicos menos sus integrantes actuales (HU12). */
    suspend fun getAvailableMusicians(bandId: Int): List<Artist.Musician>

    /** Agrega los músicos a la banda y devuelve la banda con sus integrantes actualizados (HU12). */
    suspend fun addMusiciansToBand(bandId: Int, musicianIds: List<Int>): Artist.Band
}
