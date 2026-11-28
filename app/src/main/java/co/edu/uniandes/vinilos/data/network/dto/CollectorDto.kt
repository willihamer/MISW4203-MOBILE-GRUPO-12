package co.edu.uniandes.vinilos.data.network.dto

/**
 * Forma del JSON de coleccionista en GET /collectors y GET /collectors/{id}.
 * Todos los campos son anulables para Gson; el mapper decide los valores por defecto.
 */
data class CollectorDto(
    val id: Int?,
    val name: String?,
    val telephone: String?,
    val email: String?,
    /** Músicos y bandas favoritos; con birthDate o creationDate según el tipo. */
    val favoritePerformers: List<PerformerDto>? = null,
)

/** Elemento de GET /collectors/{id}/albums: el álbum completo con su precio y estado. */
data class CollectorAlbumDto(
    val id: Int?,
    val price: Double?,
    val status: String?,
    val album: AlbumDto?,
)

/** Body de POST /collectors/{id}/albums/{albumId} (HU11); status es "Active" o "Inactive". */
data class AddCollectorAlbumRequestDto(
    val price: Double,
    val status: String,
)
