package co.edu.uniandes.vinilos.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarsTest {

    @Test
    fun `iniciales de las dos primeras palabras`() {
        assertEquals("MB", Avatars.initials("Manolo Bellon"))
        assertEquals("JM", Avatars.initials("  jaime   monsalve  "))
        assertEquals("C", Avatars.initials("Cher"))
    }

    @Test
    fun `nombre corto con la inicial del apellido`() {
        assertEquals("Martín G.", Avatars.shortName("Martín Gómez"))
        assertEquals("Cher", Avatars.shortName("Cher"))
    }

    @Test
    fun `la busqueda ignora tildes y mayusculas`() {
        assertEquals("ruben blades", "  Rubén BLADES ".normalizedForSearch())
    }
}
