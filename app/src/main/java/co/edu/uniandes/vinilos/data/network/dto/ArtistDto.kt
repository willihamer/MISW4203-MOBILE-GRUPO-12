package co.edu.uniandes.vinilos.data.network.dto

/**
 * Forma del JSON de músico que entrega BackVynils en GET /musicians y GET /musicians/{id}.
 * Todos los campos son anulables para Gson; el mapper decide los valores por defecto.
 */
data class MusicianDto(
    val id: Int?,
    val name: String?,
    val image: String?,
    val description: String?,
    val birthDate: String?,
    /** Álbumes del músico (incluidos en la respuesta del backend). */
    val albums: List<AlbumDto>? = null,
)

/**
 * Forma del JSON de banda que entrega BackVynils en GET /bands y GET /bands/{id}.
 * Todos los campos son anulables para Gson; el mapper decide los valores por defecto.
 */
data class BandDto(
    val id: Int?,
    val name: String?,
    val image: String?,
    val description: String?,
    val creationDate: String?,
    /** Álbumes de la banda (incluidos en la respuesta del backend). */
    val albums: List<AlbumDto>? = null,
    /** Integrantes de la banda (incluidos en la respuesta del backend). */
    val musicians: List<MusicianDto>? = null,
)
