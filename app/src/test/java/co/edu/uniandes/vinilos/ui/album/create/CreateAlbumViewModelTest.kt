package co.edu.uniandes.vinilos.ui.album.create

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Genre
import co.edu.uniandes.vinilos.data.model.RecordLabel
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.data.repository.AlbumRepository
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
import java.time.LocalDate

class CreateAlbumViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeVinilosApi()
    private val repository = AlbumRepository(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher)

    private fun createViewModel() = CreateAlbumViewModel(repository)

    @Test
    fun `guardar con todos los campos vacios marca error en los seis campos y no llama a la API`() {
        val viewModel = createViewModel()

        viewModel.submit()

        val state = checkNotNull(viewModel.formState.value)
        assertEquals(R.string.album_create_error_required, state.nameError)
        assertEquals(R.string.album_create_error_required, state.coverError)
        assertEquals(R.string.album_create_error_required, state.releaseDateError)
        assertEquals(R.string.album_create_error_required, state.genreError)
        assertEquals(R.string.album_create_error_required, state.recordLabelError)
        assertEquals(R.string.album_create_error_required, state.descriptionError)
        assertEquals(SaveAlbumUiState.Idle, viewModel.saveState.value)
        assertEquals(0, api.createAlbumCalls)
    }

    @Test
    fun `valida el formato HTTP o HTTPS de la URL de portada`() {
        val viewModel = createViewModel()
        fillValidFields(viewModel)
        viewModel.onCoverUrlChanged("ftp://ejemplo.com/portada.jpg")

        viewModel.submit()

        val state = checkNotNull(viewModel.formState.value)
        assertEquals(R.string.album_create_error_cover_url, state.coverError)
        assertFalse(state.hasValidCoverPreview)
        assertEquals(0, api.createAlbumCalls)
    }

    @Test
    fun `modificar un campo con error limpia su mensaje de validacion`() {
        val viewModel = createViewModel()
        viewModel.submit()

        viewModel.onNameChanged("Siembra")
        assertNull(viewModel.formState.value?.nameError)

        viewModel.onCoverUrlChanged("https://ejemplo.com/siembra.jpg")
        assertNull(viewModel.formState.value?.coverError)
        assertTrue(viewModel.formState.value?.hasValidCoverPreview == true)

        viewModel.onReleaseDateSelected(LocalDate.of(1978, 9, 7))
        assertNull(viewModel.formState.value?.releaseDateError)

        viewModel.onGenreSelected(Genre.SALSA)
        assertNull(viewModel.formState.value?.genreError)

        viewModel.onRecordLabelSelected(RecordLabel.FANIA_RECORDS)
        assertNull(viewModel.formState.value?.recordLabelError)

        viewModel.onDescriptionChanged("Clásico de la salsa.")
        assertNull(viewModel.formState.value?.descriptionError)
    }

    @Test
    fun `guardar con los seis campos validos crea el album y emite Success`() {
        val viewModel = createViewModel()
        fillValidFields(viewModel)

        viewModel.submit()

        assertEquals(1, api.createAlbumCalls)
        assertEquals("Siembra", api.lastCreatedAlbumRequest?.name)
        assertEquals("https://ejemplo.com/siembra.jpg", api.lastCreatedAlbumRequest?.cover)
        assertEquals("1984-04-15", api.lastCreatedAlbumRequest?.releaseDate)
        assertEquals("Salsa", api.lastCreatedAlbumRequest?.genre)
        assertEquals("Elektra", api.lastCreatedAlbumRequest?.recordLabel)
        assertEquals("Reseña histórica del álbum.", api.lastCreatedAlbumRequest?.description)

        val saveState = viewModel.saveState.value
        assertTrue(saveState is SaveAlbumUiState.Success)
        assertEquals("Siembra", (saveState as SaveAlbumUiState.Success).album.name)
    }

    @Test
    fun `error del servidor o sin conexion emite Error y permite reintentar`() {
        api.createAlbumResponse = { throw IOException("sin red") }
        val viewModel = createViewModel()
        fillValidFields(viewModel)

        viewModel.submit()
        assertEquals(SaveAlbumUiState.Error(R.string.error_no_connection), viewModel.saveState.value)

        api.createAlbumResponse = { throw HttpException(Response.error<Any>(500, "".toResponseBody())) }
        viewModel.submit()
        assertEquals(SaveAlbumUiState.Error(R.string.error_server), viewModel.saveState.value)
    }

    private fun fillValidFields(viewModel: CreateAlbumViewModel) {
        viewModel.onNameChanged("  Siembra  ")
        viewModel.onCoverUrlChanged(" https://ejemplo.com/siembra.jpg ")
        viewModel.onReleaseDateSelected(LocalDate.of(1984, 4, 15))
        viewModel.onGenreSelected(Genre.SALSA)
        viewModel.onRecordLabelSelected(RecordLabel.ELEKTRA)
        viewModel.onDescriptionChanged(" Reseña histórica del álbum. ")
    }
}
