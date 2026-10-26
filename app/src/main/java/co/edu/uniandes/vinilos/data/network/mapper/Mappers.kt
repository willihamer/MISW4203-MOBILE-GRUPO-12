package co.edu.uniandes.vinilos.data.network.mapper

import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.model.Genre
import co.edu.uniandes.vinilos.data.model.Performer
import co.edu.uniandes.vinilos.data.network.dto.AlbumDto
import co.edu.uniandes.vinilos.data.network.dto.CollectorDto
import co.edu.uniandes.vinilos.data.network.dto.PerformerDto
import java.time.LocalDate
import java.time.format.DateTimeParseException

fun AlbumDto.toModel() = Album(
    id = id ?: 0,
    name = name.orEmpty(),
    cover = cover.orEmpty(),
    releaseDate = parseIsoDate(releaseDate),
    description = description.orEmpty(),
    genre = Genre.fromApi(genre),
    recordLabel = recordLabel.orEmpty(),
    performers = performers.orEmpty().map { it.toModel() },
)

fun PerformerDto.toModel() = Performer(
    id = id ?: 0,
    name = name.orEmpty(),
    image = image.orEmpty(),
)

fun CollectorDto.toModel() = Collector(
    id = id ?: 0,
    name = name.orEmpty(),
    telephone = telephone.orEmpty(),
    email = email.orEmpty(),
)

/**
 * El backend serializa las fechas con hora ("1984-08-01T00:00:00.000Z"); solo se toma
 * la parte de la fecha. Una fecha inválida no debe tumbar el listado, así que da null.
 */
internal fun parseIsoDate(value: String?): LocalDate? {
    if (value.isNullOrBlank() || value.length < 10) return null
    return try {
        LocalDate.parse(value.substring(0, 10))
    } catch (e: DateTimeParseException) {
        null
    }
}
