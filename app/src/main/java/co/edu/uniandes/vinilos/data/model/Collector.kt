package co.edu.uniandes.vinilos.data.model

data class Collector(
    val id: Int,
    val name: String,
    val telephone: String,
    val email: String,
)

/**
 * Detalle de un coleccionista (HU06): datos de contacto, artistas favoritos y álbumes de su
 * colección. Combina GET /collectors/{id} y GET /collectors/{id}/albums.
 */
data class CollectorDetail(
    val id: Int,
    val name: String,
    val telephone: String,
    val email: String,
    val favoritePerformers: List<Performer> = emptyList(),
    val albums: List<CollectorAlbum> = emptyList(),
)

/** Álbum de la colección con el precio y el estado que registró el coleccionista. */
data class CollectorAlbum(
    val album: Album,
    val price: Double?,
    val status: String,
)
