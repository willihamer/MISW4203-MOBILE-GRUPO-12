package co.edu.uniandes.vinilos.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.edu.uniandes.vinilos.data.model.Profile

/** Guarda el perfil elegido para que sobreviva al cierre de la app. */
class ProfileRepository(private val preferences: SharedPreferences) {

    private val _profile = MutableLiveData(read())
    val profile: LiveData<Profile?> = _profile

    val current: Profile? get() = _profile.value

    fun selectVisitor() = save(Profile.Visitor)

    fun selectCollector(collectorId: Int, name: String) = save(Profile.CollectorProfile(collectorId, name))

    fun clear() {
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
        const val PREFERENCES_NAME = "vinilos_profile"
        private const val KEY_TYPE = "type"
        private const val KEY_COLLECTOR_ID = "collector_id"
        private const val KEY_COLLECTOR_NAME = "collector_name"
        private const val TYPE_VISITOR = "visitor"
        private const val TYPE_COLLECTOR = "collector"
    }
}
