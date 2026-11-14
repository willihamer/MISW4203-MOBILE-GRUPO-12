package co.edu.uniandes.vinilos.data.repository

import androidx.lifecycle.LiveData
import co.edu.uniandes.vinilos.data.model.Profile

/** Contrato del repositorio que gestiona el perfil activo (visitante o coleccionista). */
interface ProfileRepository {
    val profile: LiveData<Profile?>
    val current: Profile?

    fun selectVisitor()

    fun selectCollector(collectorId: Int, name: String)

    fun clear()

    companion object {
        const val PREFERENCES_NAME = "vinilos_profile"
    }
}
