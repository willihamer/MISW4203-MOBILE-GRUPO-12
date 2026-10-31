package co.edu.uniandes.vinilos.ui.common

import java.text.Normalizer
import java.util.Locale

/** Normaliza para buscar sin distinguir mayúsculas ni tildes ("america" encuentra "América"). */
fun String.normalizedForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase(Locale.ROOT)
        .trim()
