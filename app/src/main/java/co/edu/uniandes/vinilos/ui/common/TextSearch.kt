package co.edu.uniandes.vinilos.ui.common

import java.text.Normalizer
import java.util.Locale

/** Marcas diacríticas (tildes) que deja la forma NFD; se compila una sola vez y no en cada llamada. */
private val DIACRITICS = Regex("\\p{Mn}+")

/** Normaliza para buscar sin distinguir mayúsculas ni tildes ("america" encuentra "América"). */
fun String.normalizedForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(DIACRITICS, "")
        .lowercase(Locale.ROOT)
        .trim()
