package co.edu.uniandes.vinilos.testutil

import co.edu.uniandes.vinilos.data.network.VinilosApi
import co.edu.uniandes.vinilos.data.network.dto.AlbumDto
import co.edu.uniandes.vinilos.data.network.dto.CollectorDto
import co.edu.uniandes.vinilos.data.network.dto.PerformerDto

/** Doble de la API: cada prueba decide qué responde o qué excepción lanza. */
class FakeVinilosApi(
    var albums: () -> List<AlbumDto> = { emptyList() },
    var collectors: () -> List<CollectorDto> = { emptyList() },
) : VinilosApi {
    var albumCalls = 0
        private set

    override suspend fun getAlbums(): List<AlbumDto> {
        albumCalls++
        return albums()
    }

    override suspend fun getCollectors(): List<CollectorDto> = collectors()
}

/** Álbumes con la misma forma que los datos de prueba de BackVynils. */
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
}
