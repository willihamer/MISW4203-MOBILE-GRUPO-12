package co.edu.uniandes.vinilos.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextSearchTest {

    @Test
    fun `normaliza sin tildes ni mayusculas`() {
        assertEquals("ruben blades", "  Rubén BLADES ".normalizedForSearch())
    }

    @Test
    fun `la clave de busqueda incluye todos los campos ya normalizados`() {
        val (item, key) = listOf("Buscando América").withSearchKeys { listOf(it, "Elektra", "Rubén Blades") }.single()

        assertEquals("Buscando América", item)
        assertTrue(key.contains("america"))
        assertTrue(key.contains("elektra"))
        assertTrue(key.contains("ruben"))
    }

    @Test
    fun `una busqueda no coincide con el final de un campo y el inicio del siguiente`() {
        val key = listOf(Unit).withSearchKeys { listOf("Salsa", "Elektra") }.single().key

        assertFalse(key.contains("salsaelektra"))
        assertFalse(key.contains("salsa elektra"))
    }
}
