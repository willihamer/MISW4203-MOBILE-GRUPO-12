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

/** Artista (músico o banda) tal como viene dentro de un álbum. */
data class Performer(
    val id: Int,
    val name: String,
    val image: String,
)
