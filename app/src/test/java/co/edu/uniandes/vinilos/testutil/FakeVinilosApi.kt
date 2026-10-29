package co.edu.uniandes.vinilos.testutil

import co.edu.uniandes.vinilos.data.network.VinilosApi
import co.edu.uniandes.vinilos.data.network.dto.AlbumDto
import co.edu.uniandes.vinilos.data.network.dto.BandDto
import co.edu.uniandes.vinilos.data.network.dto.CollectorDto
import co.edu.uniandes.vinilos.data.network.dto.MusicianDto
import co.edu.uniandes.vinilos.data.network.dto.PerformerDto

/** Doble de la API: cada prueba decide qué responde o qué excepción lanza. */
class FakeVinilosApi(
    var albums: () -> List<AlbumDto> = { emptyList() },
    var collectors: () -> List<CollectorDto> = { emptyList() },
    var musicians: () -> List<MusicianDto> = { emptyList() },
    var bands: () -> List<BandDto> = { emptyList() },
) : VinilosApi {
    var albumCalls = 0
        private set
    var musicianCalls = 0
        private set
    var bandCalls = 0
        private set

    override suspend fun getAlbums(): List<AlbumDto> {
        albumCalls++
        return albums()
    }

    override suspend fun getCollectors(): List<CollectorDto> = collectors()

    override suspend fun getMusicians(): List<MusicianDto> {
        musicianCalls++
        return musicians()
    }

    override suspend fun getBands(): List<BandDto> {
        bandCalls++
        return bands()
    }
}

/** Datos con la misma forma que los datos de prueba de BackVynils. */
object SampleData {
    val buscandoAmerica = AlbumDto(
        id = 100,
        name = "Buscando América",
        cover = "https://example.com/buscando.jpg",
        releaseDate = "1984-08-01T00:00:00.000Z",
        description = "Primer álbum de Rubén Blades y Seis del Solar.",
        genre = "Salsa",
        recordLabel = "Elektra",
        performers = listOf(PerformerDto(100, "Rubén Blades Bellido de Luna", "https://example.com/ruben.jpg")),
    )

    val nightAtTheOpera = AlbumDto(
        id = 102,
        name = "A Night at the Opera",
        cover = "https://example.com/opera.png",
        releaseDate = "1975-11-21T00:00:00.000Z",
        description = "Cuarto álbum de estudio de Queen.",
        genre = "Rock",
        recordLabel = "EMI",
        performers = listOf(PerformerDto(101, "Queen", "https://example.com/queen.jpg")),
    )

    val albums = listOf(buscandoAmerica, nightAtTheOpera)

    val rubenBlades = MusicianDto(
        id = 100,
        name = "Rubén Blades Bellido de Luna",
        image = "https://example.com/ruben.jpg",
        description = "Cantante y compositor panameño.",
        birthDate = "1948-07-16T00:00:00.000Z",
    )

    val willieColon = MusicianDto(
        id = 102,
        name = "Willie Colón",
        image = "https://example.com/willie.jpg",
        description = "Trombonista y cantante de salsa.",
        birthDate = "1950-04-28T00:00:00.000Z",
    )

    val queen = BandDto(
        id = 101,
        name = "Queen",
        image = "https://example.com/queen.jpg",
        description = "Banda británica de rock formada en 1970.",
        creationDate = "1970-01-01T00:00:00.000Z",
    )

    val faniaAllStars = BandDto(
        id = 103,
        name = "Fania All-Stars",
        image = "https://example.com/fania.jpg",
        description = "Grupo de música latina.",
        creationDate = "1968-01-01T00:00:00.000Z",
    )

    val musicians = listOf(rubenBlades, willieColon)
    val bands = listOf(queen, faniaAllStars)
}
