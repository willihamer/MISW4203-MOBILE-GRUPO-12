package co.edu.uniandes.vinilos.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

/** #119: todas las duraciones se guardan y se muestran con el formato único mm:ss. */
class TrackDurationTest {

    @Test
    fun `los minutos de un digito se completan con un cero`() {
        assertEquals("05:05", formatTrackDuration("5:05"))
        assertEquals("03:20", formatTrackDuration("3:20"))
        assertEquals("00:01", formatTrackDuration("0:01"))
    }

    @Test
    fun `una duracion que ya tiene el formato mm ss no cambia`() {
        assertEquals("04:24", formatTrackDuration("04:24"))
        assertEquals("59:59", formatTrackDuration("59:59"))
    }

    @Test
    fun `quita los espacios a los lados`() {
        assertEquals("06:29", formatTrackDuration(" 6:29 "))
    }

    @Test
    fun `un valor sin el formato esperado se devuelve sin cambios`() {
        listOf("", "abc", "123:00", "4.35").forEach { assertEquals(it, it, formatTrackDuration(it)) }
    }
}
