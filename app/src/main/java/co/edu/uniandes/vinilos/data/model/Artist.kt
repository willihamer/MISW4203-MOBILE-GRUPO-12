package co.edu.uniandes.vinilos.data.model

import java.time.LocalDate

/** Tipo de artista para la categorización en catálogo y filtros. */
enum class ArtistType {
    MUSICIAN,
    BAND,
}

/**
 * Representación de un artista del catálogo (músico individual o banda).
 * Se utiliza en el listado de artistas (HU03) y en el detalle (HU04).
 */
sealed class Artist(
    open val id: Int,
    open val name: String,
    open val image: String,
    open val description: String,
    open val type: ArtistType,
) {
    data class Musician(
        override val id: Int,
        override val name: String,
        override val image: String,
        override val description: String,
        val birthDate: LocalDate? = null,
    ) : Artist(id, name, image, description, ArtistType.MUSICIAN)

    data class Band(
        override val id: Int,
        override val name: String,
        override val image: String,
        override val description: String,
        val creationDate: LocalDate? = null,
    ) : Artist(id, name, image, description, ArtistType.BAND)
}
