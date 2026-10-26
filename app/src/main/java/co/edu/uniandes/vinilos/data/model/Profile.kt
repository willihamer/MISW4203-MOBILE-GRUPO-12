package co.edu.uniandes.vinilos.data.model

/**
 * Perfil con el que se usa la app. El backend no tiene autenticación: el coleccionista
 * solo indica quién es para que las HU de escritura sepan en nombre de quién actúan.
 */
sealed interface Profile {
    data object Visitor : Profile
    data class CollectorProfile(val collectorId: Int, val name: String) : Profile
}
