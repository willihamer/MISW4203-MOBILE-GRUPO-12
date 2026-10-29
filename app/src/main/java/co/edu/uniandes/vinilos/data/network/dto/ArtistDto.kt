package co.edu.uniandes.vinilos.data.network.dto

/**
 * Forma del JSON de músico que entrega BackVynils en GET /musicians.
 * Todos los campos son anulables para Gson; el mapper decide los valores por defecto.
 */
data class MusicianDto(
    val id: Int?,
    val name: String?,
    val image: String?,
    val description: String?,
    val birthDate: String?,
)

/**
 * Forma del JSON de banda que entrega BackVynils en GET /bands.
 * Todos los campos son anulables para Gson; el mapper decide los valores por defecto.
 */
data class BandDto(
    val id: Int?,
    val name: String?,
    val image: String?,
    val description: String?,
    val creationDate: String?,
)
