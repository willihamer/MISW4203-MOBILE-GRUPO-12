package co.edu.uniandes.vinilos.data.network.dto

/**
 * Forma exacta del JSON de BackVynils. Todos los campos son anulables porque Gson no
 * respeta la nulabilidad de Kotlin; el mapper decide los valores por defecto.
 */
data class AlbumDto(
    val id: Int?,
    val name: String?,
    val cover: String?,
    /** Fecha ISO con hora, p. ej. "1984-08-01T00:00:00.000Z". */
    val releaseDate: String?,
    val description: String?,
    val genre: String?,
    val recordLabel: String?,
    val performers: List<PerformerDto>?,
    val tracks: List<TrackDto>? = null,
)

data class PerformerDto(
    val id: Int?,
    val name: String?,
    val image: String?,
    val birthDate: String? = null,
    val creationDate: String? = null,
)

data class TrackDto(val id: Int?, val name: String?, val duration: String?)
