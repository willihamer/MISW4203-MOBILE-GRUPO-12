package co.edu.uniandes.vinilos.ui.album.track

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.data.network.dto.TrackDto
import co.edu.uniandes.vinilos.data.repository.AlbumRepository
import co.edu.uniandes.vinilos.data.repository.AlbumRepositoryImpl
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AddTrackViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeVinilosApi()
    private val repository: AlbumRepository = AlbumRepositoryImpl(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher)

    private fun createViewModel(albumId: Int = 100) = AddTrackViewModel(
        repository,
        SavedStateHandle(mapOf(AddTrackViewModel.ARG_ALBUM_ID to albumId)),
    )

    @Test
    fun `guardar con los campos vacios marca ambos como obligatorios y no llama a la API`() {
        val viewModel = createViewModel()

        viewModel.submit()

        val state = checkNotNull(viewModel.formState.value)
        assertEquals(R.string.error_required_field, state.nameError)
        assertEquals(R.string.error_required_field, state.durationError)
        assertEquals(SaveTrackUiState.Idle, viewModel.saveState.value)
        assertEquals(0, api.addTrackCalls)
    }

    @Test
    fun `una duracion fuera del formato mm ss muestra su error y no llama a la API`() {
        listOf("5:5", "abc", "03:75", "4.35", "123:00", "00:00").forEach { duration ->
            val viewModel = createViewModel()
            viewModel.onNameChanged("Decisiones")
            viewModel.onDurationChanged(duration)

            viewModel.submit()

            assertEquals(duration, R.string.track_add_error_duration_format, viewModel.formState.value?.durationError)
        }
        assertEquals(0, api.addTrackCalls)
    }

    @Test
    fun `acepta minutos de uno o dos digitos con segundos entre 00 y 59`() {
        listOf("5:05", "04:35", "59:59", "0:01").forEach { assertTrue(it, isValidTrackDuration(it)) }
        listOf("", "5:60", "-1:00", "05:5", " :30").forEach { assertFalse(it, isValidTrackDuration(it)) }
    }

    @Test
    fun `corregir un campo limpia solo su error`() {
        val viewModel = createViewModel()
        viewModel.submit()

        viewModel.onNameChanged("Decisiones")

        val state = checkNotNull(viewModel.formState.value)
        assertNull(state.nameError)
        assertEquals(R.string.error_required_field, state.durationError)
    }

    @Test
    fun `guardar con datos validos envia nombre y duracion recortados al album y termina en exito`() {
        val viewModel = createViewModel(albumId = 101)
        viewModel.onNameChanged("  Decisiones ")
        viewModel.onDurationChanged(" 5:05 ")

        viewModel.submit()

        assertEquals(101, api.lastAddTrackAlbumId)
        assertEquals("Decisiones", api.lastAddTrackRequest?.name)
        assertEquals("5:05", api.lastAddTrackRequest?.duration)
        val success = viewModel.saveState.value as SaveTrackUiState.Success
        assertEquals("Decisiones", success.track.name)
    }

    @Test
    fun `un error del servidor se muestra y al corregir un campo se puede reintentar`() {
        api.addTrackResponse = { _, _ -> throw HttpException(Response.error<Any>(500, "".toResponseBody())) }
        val viewModel = createViewModel()
        viewModel.onNameChanged("Decisiones")
        viewModel.onDurationChanged("5:05")

        viewModel.submit()
        assertEquals(SaveTrackUiState.Error(R.string.error_server), viewModel.saveState.value)

        viewModel.onNameChanged("Desapariciones")
        assertEquals(SaveTrackUiState.Idle, viewModel.saveState.value)

        api.addTrackResponse = { _, req -> TrackDto(301, req.name, req.duration) }
        viewModel.submit()
        assertTrue(viewModel.saveState.value is SaveTrackUiState.Success)
        assertEquals(2, api.addTrackCalls)
    }

    @Test
    fun `sin conexion muestra el mensaje de red`() {
        api.addTrackResponse = { _, _ -> throw IOException("sin red") }
        val viewModel = createViewModel()
        viewModel.onNameChanged("Decisiones")
        viewModel.onDurationChanged("5:05")

        viewModel.submit()

        assertEquals(SaveTrackUiState.Error(R.string.error_no_connection), viewModel.saveState.value)
    }

    @Test
    fun `mientras guarda ignora un segundo envio`() {
        val viewModel = createViewModel()
        viewModel.onNameChanged("Decisiones")
        viewModel.onDurationChanged("5:05")
        api.addTrackResponse = { _, req ->
            // Simula un doble toque mientras la petición sigue en curso
            viewModel.submit()
            TrackDto(300, req.name, req.duration)
        }

        viewModel.submit()

        assertEquals(1, api.addTrackCalls)
    }
}
