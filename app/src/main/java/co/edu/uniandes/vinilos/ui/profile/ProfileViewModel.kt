package co.edu.uniandes.vinilos.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.model.Profile
import co.edu.uniandes.vinilos.data.repository.ProfileRepository

/** Perfil activo (visitante o coleccionista), compartido por las pantallas de la Activity. */
class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    val profile: LiveData<Profile?> = repository.profile

    fun enterAsVisitor() = repository.selectVisitor()

    fun enterAsCollector(collector: Collector) = repository.selectCollector(collector.id, collector.name)

    fun changeProfile() = repository.clear()
}
