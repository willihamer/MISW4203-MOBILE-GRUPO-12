package co.edu.uniandes.vinilos.data.model

import java.time.LocalDate

data class Album(
    val id: Int,
    val name: String,
    val cover: String,
    val releaseDate: LocalDate?,
    val description: String,
    val genre: Genre?,
    val recordLabel: String,
    val performers: List<Performer> = emptyList(),
    val tracks: List<Track> = emptyList(),
) {
    val releaseYear: Int? get() = releaseDate?.year
}

/** Valores cerrados del backend (BackVynils, enum GENRE). */
enum class Genre(val apiValue: String) {
    CLASSICAL("Classical"),
    SALSA("Salsa"),
    ROCK("Rock"),
    FOLK("Folk");

    companion object {
        fun fromApi(value: String?): Genre? = entries.firstOrNull { it.apiValue == value }
    }
}

/** Valores cerrados del backend (BackVynils, enum RECORD_LABEL). */
enum class RecordLabel(val apiValue: String) {
    SONY_MUSIC("Sony Music"),
    EMI("EMI"),
    DISCOS_FUENTES("Discos Fuentes"),
    ELEKTRA("Elektra"),
    FANIA_RECORDS("Fania Records");

    companion object {
        fun fromApi(value: String?): RecordLabel? = entries.firstOrNull { it.apiValue == value }
    }
}

/** Datos obligatorios para crear un álbum en el catálogo (HU07, POST /albums). */
data class NewAlbum(
    val name: String,
    val cover: String,
    val releaseDate: LocalDate,
    val description: String,
    val genre: Genre,
    val recordLabel: RecordLabel,
)

/** Artista (músico o banda) tal como viene dentro de un álbum. */
data class Performer(
    val id: Int,
    val name: String,
    val image: String,
    val type: ArtistType? = null,
)

data class Track(val id: Int, val name: String, val duration: String)

/** Datos obligatorios para asociar un track a un álbum (HU08, POST /albums/{id}/tracks). */
data class NewTrack(val name: String, val duration: String)
