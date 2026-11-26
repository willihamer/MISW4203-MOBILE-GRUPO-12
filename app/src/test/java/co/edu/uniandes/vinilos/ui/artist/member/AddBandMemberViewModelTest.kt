package co.edu.uniandes.vinilos.ui.artist.member

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.data.repository.ArtistRepositoryImpl
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.testutil.SampleData
import co.edu.uniandes.vinilos.ui.common.UiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class AddBandMemberViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeVinilosApi(
        musicians = { SampleData.musicians },
        bands = { SampleData.bands },
    )

    private fun createViewModel() = AddBandMemberViewModel(
        ArtistRepositoryImpl(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher),
        SavedStateHandle(mapOf(AddBandMemberViewModel.ARG_BAND_ID to 101)),
    )

    private fun AddBandMemberViewModel.names() =
        (state.value as UiState.Success<List<Artist.Musician>>).data.map { it.name }

    @Test
    fun `lista solo los musicos que no pertenecen a la banda`() {
        api.band = { SampleData.queenDetail.copy(musicians = listOf(SampleData.willieColon)) }

        assertEquals(listOf("Rubén Blades Bellido de Luna"), createViewModel().names())
    }

    @Test
    fun `si todos los musicos ya son integrantes el estado es vacio`() {
        api.band = { SampleData.queenDetail.copy(musicians = SampleData.musicians) }

        assertEquals(UiState.Empty, createViewModel().state.value)
    }

    @Test
    fun `la busqueda no distingue tildes ni mayusculas y conserva la seleccion`() {
        val viewModel = createViewModel()
        viewModel.onMusicianClicked(102)

        viewModel.onQueryChanged("RUBEN")

        assertEquals(listOf("Rubén Blades Bellido de Luna"), viewModel.names())
        assertEquals(setOf(102), viewModel.selected.value)
        viewModel.onQueryChanged("zzz")
        assertTrue(viewModel.names().isEmpty())
    }

    @Test
    fun `tocar un musico lo selecciona y tocarlo otra vez lo quita`() {
        val viewModel = createViewModel()

        viewModel.onMusicianClicked(100)
        viewModel.onMusicianClicked(102)
        viewModel.onMusicianClicked(100)

        assertEquals(setOf(102), viewModel.selected.value)
    }

    @Test
    fun `sin seleccion no se envia nada`() {
        val viewModel = createViewModel()

        viewModel.submit()

        assertEquals(SaveMembersUiState.Idle, viewModel.saveState.value)
        assertTrue(api.addedBandMembers.isEmpty())
    }

    @Test
    fun `agregar envia los musicos seleccionados y devuelve la banda actualizada`() {
        val viewModel = createViewModel()
        viewModel.onMusicianClicked(100)
        viewModel.onMusicianClicked(102)
        api.band = { SampleData.queenDetail.copy(musicians = listOf(SampleData.freddieMercury, SampleData.rubenBlades, SampleData.willieColon)) }

        viewModel.submit()

        assertEquals(setOf(101 to 100, 101 to 102), api.addedBandMembers.toSet())
        val success = viewModel.saveState.value as SaveMembersUiState.Success
        assertEquals(2, success.added)
        assertEquals(3, success.band.musicians.size)
    }

    @Test
    fun `un error al guardar se muestra y al cambiar la seleccion se puede reintentar`() {
        val viewModel = createViewModel()
        viewModel.onMusicianClicked(100)
        api.addMusicianToBandResponse = { _, _ -> throw IOException("sin red") }

        viewModel.submit()
        assertEquals(SaveMembersUiState.Error(R.string.error_no_connection), viewModel.saveState.value)

        viewModel.onMusicianClicked(102)
        assertEquals(SaveMembersUiState.Idle, viewModel.saveState.value)
    }

    @Test
    fun `mientras guarda ignora un segundo envio y los cambios de seleccion`() {
        val viewModel = createViewModel()
        viewModel.onMusicianClicked(100)
        api.addMusicianToBandResponse = { _, musicianId ->
            // Simula un doble toque y un cambio de selección mientras la petición sigue en curso
            viewModel.submit()
            viewModel.onMusicianClicked(102)
            SampleData.musicians.first { it.id == musicianId }
        }

        viewModel.submit()

        assertEquals(listOf(101 to 100), api.addedBandMembers)
        assertEquals(setOf(100), viewModel.selected.value)
    }

    @Test
    fun `sin conexion al cargar muestra el error y reintentar carga la lista`() {
        api.musicians = { throw IOException("sin red") }
        val viewModel = createViewModel()
        assertEquals(UiState.Error(R.string.error_no_connection), viewModel.state.value)

        api.musicians = { SampleData.musicians }
        viewModel.load()

        assertEquals(listOf("Rubén Blades Bellido de Luna", "Willie Colón"), viewModel.names())
    }
}
