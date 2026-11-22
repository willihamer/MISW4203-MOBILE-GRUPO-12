package co.edu.uniandes.vinilos.ui.common

import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import androidx.core.graphics.toColorInt

/** Avatar con iniciales sobre un gradiente, como en P01b y en el chip de perfil. */
object Avatars {

    private val gradients by lazy {
        listOf(
            intArrayOf("#6930BC".toColorInt(), "#006377".toColorInt()),
            intArrayOf("#A32261".toColorInt(), "#855000".toColorInt()),
            intArrayOf("#006377".toColorInt(), "#006344".toColorInt()),
            intArrayOf("#BF2076".toColorInt(), "#7C3AED".toColorInt()),
        )
    }

    fun initials(name: String): String = name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    /** "Martín Gómez" → "Martín G." */
    fun shortName(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return if (parts.size < 2) name.trim() else "${parts[0]} ${parts[1].first().uppercase()}."
    }

    fun bind(view: TextView, name: String, seed: Int, circle: Boolean) {
        view.text = initials(name)
        view.background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            gradients[Math.floorMod(seed, gradients.size)],
        ).apply {
            if (circle) {
                shape = GradientDrawable.OVAL
            } else {
                cornerRadius = 14f * view.resources.displayMetrics.density
            }
        }
    }
}
