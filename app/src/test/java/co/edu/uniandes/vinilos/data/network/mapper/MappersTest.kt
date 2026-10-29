package co.edu.uniandes.vinilos.data.network.mapper

import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.data.model.Genre
import co.edu.uniandes.vinilos.data.network.dto.AlbumDto
import co.edu.uniandes.vinilos.data.network.dto.BandDto
import co.edu.uniandes.vinilos.data.network.dto.MusicianDto
import co.edu.uniandes.vinilos.testutil.SampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MappersTest {

    @Test
    fun `convierte la fecha con hora del backend en LocalDate`() {
        assertEquals(LocalDate.of(1984, 8, 1), parseIsoDate("1984-08-01T00:00:00.000Z"))
    }

    @Test
    fun `una fecha invalida o ausente da null`() {
        assertNull(parseIsoDate(null))
        assertNull(parseIsoDate(""))
        assertNull(parseIsoDate("1984"))
        assertNull(parseIsoDate("no-es-fecha"))
    }

    @Test
    fun `mapea el album con genero, sello, anio y artistas`() {
        val album = SampleData.buscandoAmerica.toModel()

        assertEquals(100, album.id)
        assertEquals("Buscando América", album.name)
        assertEquals(Genre.SALSA, album.genre)
        assertEquals("Elektra", album.recordLabel)
        assertEquals(1984, album.releaseYear)
        assertEquals(listOf("Rubén Blades Bellido de Luna"), album.performers.map { it.name })
    }

    @Test
    fun `campos ausentes no rompen el mapeo`() {
        val album = AlbumDto(null, null, null, null, null, "Reggaeton", null, null).toModel()

        assertEquals("", album.name)
        assertNull(album.genre)
        assertNull(album.releaseYear)
        assertTrue(album.performers.isEmpty())
    }

    @Test
    fun `mapea MusicianDto a Artist Musician`() {
        val musician = SampleData.rubenBlades.toModel()

        assertEquals(100, musician.id)
        assertEquals("Rubén Blades Bellido de Luna", musician.name)
        assertEquals(ArtistType.MUSICIAN, musician.type)
        assertEquals(LocalDate.of(1948, 7, 16), musician.birthDate)
    }

    @Test
    fun `mapea BandDto a Artist Band`() {
        val band = SampleData.queen.toModel()

        assertEquals(101, band.id)
        assertEquals("Queen", band.name)
        assertEquals(ArtistType.BAND, band.type)
        assertEquals(LocalDate.of(1970, 1, 1), band.creationDate)
    }

    @Test
    fun `mapeo de artista tolera campos nulos`() {
        val musician = MusicianDto(null, null, null, null, null).toModel()
        assertEquals(0, musician.id)
        assertEquals("", musician.name)
        assertNull(musician.birthDate)

        val band = BandDto(null, null, null, null, null).toModel()
        assertEquals(0, band.id)
        assertEquals("", band.name)
        assertNull(band.creationDate)
    }
}
