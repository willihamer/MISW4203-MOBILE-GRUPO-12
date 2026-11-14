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

/** Elemento de un listado con su texto de búsqueda ya normalizado. */
data class Searchable<T>(val item: T, val key: String)

/**
 * Normaliza los campos de búsqueda de cada elemento una sola vez, al cargar el listado, y no
 * en cada letra que escribe el usuario. El salto de línea separa los campos para que una
 * búsqueda no coincida con el final de un campo y el inicio del siguiente.
 */
fun <T> List<T>.withSearchKeys(fields: (T) -> List<String>): List<Searchable<T>> =
    map { item -> Searchable(item, fields(item).joinToString("\n") { it.normalizedForSearch() }) }
