package co.edu.uniandes.vinilos.ui.common

import androidx.core.view.isVisible
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Profile
import co.edu.uniandes.vinilos.databinding.ViewProfileChipBinding

/** Chip de perfil del encabezado: "Visitante" o las iniciales y el nombre del coleccionista. */
fun ViewProfileChipBinding.render(profile: Profile?) {
    val context = root.context
    when (profile) {
        is Profile.CollectorProfile -> {
            profileIcon.isVisible = false
            profileAvatar.isVisible = true
            Avatars.bind(profileAvatar, profile.name, profile.collectorId, circle = true)
            profileName.text = Avatars.shortName(profile.name)
            profileRole.isVisible = true
            profileRole.setText(R.string.profile_collector)
            root.contentDescription = context.getString(R.string.profile_change) + ": " + profile.name
        }
        else -> {
            profileAvatar.isVisible = false
            profileIcon.isVisible = true
            profileName.setText(R.string.profile_visitor)
            profileRole.isVisible = false
            root.contentDescription =
                context.getString(R.string.profile_change) + ": " + context.getString(R.string.profile_visitor)
        }
    }
}
