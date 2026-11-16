package co.edu.uniandes.vinilos.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.edu.uniandes.vinilos.data.model.Profile

/** Guarda el perfil elegido para que sobreviva al cierre de la app. */
class ProfileRepositoryImpl(private val preferences: SharedPreferences) : ProfileRepository {

    private val _profile = MutableLiveData(read())
    override val profile: LiveData<Profile?> = _profile

    override val current: Profile? get() = _profile.value

    override fun selectVisitor() = save(Profile.Visitor)

    override fun selectCollector(collectorId: Int, name: String) =
        save(Profile.CollectorProfile(collectorId, name))

    override fun clear() {
        preferences.edit { clear() }
        _profile.value = null
    }

    private fun save(profile: Profile) {
        preferences.edit {
            when (profile) {
                Profile.Visitor -> {
                    putString(KEY_TYPE, TYPE_VISITOR)
                    remove(KEY_COLLECTOR_ID)
                    remove(KEY_COLLECTOR_NAME)
                }
                is Profile.CollectorProfile -> {
                    putString(KEY_TYPE, TYPE_COLLECTOR)
                    putInt(KEY_COLLECTOR_ID, profile.collectorId)
                    putString(KEY_COLLECTOR_NAME, profile.name)
                }
            }
        }
        _profile.value = profile
    }

    private fun read(): Profile? = when (preferences.getString(KEY_TYPE, null)) {
        TYPE_VISITOR -> Profile.Visitor
        TYPE_COLLECTOR -> Profile.CollectorProfile(
            collectorId = preferences.getInt(KEY_COLLECTOR_ID, 0),
            name = preferences.getString(KEY_COLLECTOR_NAME, null).orEmpty(),
        )
        else -> null
    }

    companion object {
        const val PREFERENCES_NAME = ProfileRepository.PREFERENCES_NAME
        private const val KEY_TYPE = "type"
        private const val KEY_COLLECTOR_ID = "collector_id"
        private const val KEY_COLLECTOR_NAME = "collector_name"
        private const val TYPE_VISITOR = "visitor"
        private const val TYPE_COLLECTOR = "collector"
    }
}
