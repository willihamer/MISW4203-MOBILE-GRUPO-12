package co.edu.uniandes.vinilos.data.model

/** Minutos de uno o dos dígitos y segundos entre 00 y 59. */
private val TRACK_DURATION = Regex("""^(\d{1,2}):([0-5]\d)$""")

/** Una duración es válida si tiene el formato m:ss o mm:ss y no es cero. */
fun isValidTrackDuration(value: String): Boolean {
    val match = TRACK_DURATION.matchEntire(value.trim()) ?: return false
    val (minutes, seconds) = match.destructured
    return minutes.toInt() > 0 || seconds.toInt() > 0
}

/**
 * Lleva una duración al formato único mm:ss de HU08, con dos dígitos en los minutos: "5:05" pasa
 * a "05:05". Antes la app guardaba la duración tal como se escribía y el detalle mezclaba "3:20"
 * con "04:24" (#119). Se aplica al enviar un track y al mostrar los que ya están en el backend,
 * cuyos datos semilla usan un solo dígito. Si el valor no tiene el formato esperado, se devuelve
 * sin espacios a los lados y sin otros cambios.
 */
fun formatTrackDuration(value: String): String {
    val trimmed = value.trim()
    val match = TRACK_DURATION.matchEntire(trimmed) ?: return trimmed
    val (minutes, seconds) = match.destructured
    return "${minutes.padStart(2, '0')}:$seconds"
}
