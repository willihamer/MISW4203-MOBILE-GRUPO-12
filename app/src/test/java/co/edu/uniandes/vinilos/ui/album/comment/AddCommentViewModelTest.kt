package co.edu.uniandes.vinilos.ui.album.comment

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.NewComment
import co.edu.uniandes.vinilos.data.model.Profile
import co.edu.uniandes.vinilos.data.network.AppError
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.data.network.dto.CommentDto
import co.edu.uniandes.vinilos.data.repository.AlbumRepositoryImpl
import co.edu.uniandes.vinilos.data.repository.ProfileRepository
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.testutil.SampleData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class AddCommentViewModelTest {
    @get:Rule val instantTaskRule = InstantTaskExecutorRule()
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val api = FakeVinilosApi()
    private val repository = AlbumRepositoryImpl(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher)
    private val profiles = object : ProfileRepository {
        override val profile = MutableLiveData<Profile?>(Profile.CollectorProfile(7, "Ana"))
        override val current: Profile? get() = profile.value
        override fun selectVisitor() { profile.value = Profile.Visitor }
        override fun selectCollector(collectorId: Int, name: String) { profile.value = Profile.CollectorProfile(collectorId, name) }
        override fun clear() { profile.value = null }
    }
    private fun model(state: SavedStateHandle = SavedStateHandle(mapOf("albumId" to 100))) =
        AddCommentViewModel(repository, profiles, state)

    @Test fun `requiere resena y calificacion sin enviar datos invalidos`() {
        val vm = model()
        vm.onDescriptionChanged("   ")
        vm.submit()
        assertEquals(R.string.error_required_field, vm.formState.value?.descriptionError)
        assertEquals(R.string.comment_rating_required, vm.formState.value?.ratingError)
        vm.onDescriptionChanged("Buen álbum")
        listOf(-1, 0, 6).forEach { rating ->
            vm.onRatingChanged(rating)
            vm.submit()
            assertEquals(R.string.comment_rating_required, vm.formState.value?.ratingError)
        }
        assertEquals(0, api.addCommentCalls)
    }

    @Test fun `envia el album y el coleccionista activos con los extremos validos`() {
        listOf(1, 5).forEach { rating ->
            val vm = model()
            vm.onDescriptionChanged("  Buen álbum  ")
            vm.onRatingChanged(rating)
            vm.submit()
            assertEquals(SaveCommentUiState.Success, vm.saveState.value)
            assertEquals(100, api.lastCommentAlbumId)
            assertEquals(7, api.lastCommentRequest?.collector?.id)
            assertEquals("Buen álbum", api.lastCommentRequest?.description)
            assertEquals(rating, api.lastCommentRequest?.rating)
        }
    }

    @Test fun `visitante o perfil ausente no puede publicar`() {
        val vm = model()
        vm.onDescriptionChanged("Opinión")
        vm.onRatingChanged(4)
        profiles.selectVisitor()
        vm.submit()
        assertEquals(SaveCommentUiState.Error(R.string.comment_collector_required), vm.saveState.value)
        profiles.clear()
        vm.submit()
        assertEquals(0, api.addCommentCalls)
    }

    @Test fun `al publicar se usa el perfil vigente aunque cambie durante la edicion`() {
        val vm = model()
        vm.onDescriptionChanged("Reseña")
        vm.onRatingChanged(3)
        profiles.selectCollector(12, "Luis")
        vm.submit()
        assertEquals(12, api.lastCommentRequest?.collector?.id)
    }

    @Test fun `un perfil sin identificador valido no puede enviar el comentario`() {
        val vm = model()
        vm.onDescriptionChanged("Reseña")
        vm.onRatingChanged(3)
        profiles.selectCollector(0, "Ana")
        vm.submit()
        assertEquals(SaveCommentUiState.Error(R.string.comment_collector_required), vm.saveState.value)
        assertEquals(0, api.addCommentCalls)
    }

    @Test fun `no duplica envios mientras guarda ni despues de un exito`() {
        val vm = model()
        vm.onDescriptionChanged("Opinión")
        vm.onRatingChanged(4)
        api.addCommentResponse = { _, request ->
            vm.submit()
            CommentDto(400, request.description, request.rating)
        }
        vm.submit()
        vm.submit()
        assertEquals(1, api.addCommentCalls)
    }

    @Test fun `un fallo conserva el formulario y permite reintentar`() {
        val vm = model()
        vm.onDescriptionChanged("Opinión")
        vm.onRatingChanged(3)
        api.addCommentResponse = { _, _ -> throw IOException() }
        vm.submit()
        assertEquals(SaveCommentUiState.Error(R.string.error_no_connection), vm.saveState.value)
        assertEquals("Opinión", vm.formState.value?.description)
        assertEquals(3, vm.formState.value?.rating)
        api.addCommentResponse = { _, request -> CommentDto(400, request.description, request.rating) }
        vm.submit()
        assertEquals(SaveCommentUiState.Success, vm.saveState.value)
        assertEquals(2, api.addCommentCalls)
    }

    @Test fun `restaura el borrador con saved state`() {
        val state = SavedStateHandle(mapOf("albumId" to 100))
        val vm = model(state)
        vm.onDescriptionChanged("Mi reseña")
        vm.onRatingChanged(2)
        val restored = model(state)
        assertEquals("Mi reseña", restored.formState.value?.description)
        assertEquals(2, restored.formState.value?.rating)
    }

    @Test fun `el comentario se agrega a la cache sin otra consulta de detalle`() = runTest {
        api.album = { SampleData.buscandoAmerica.copy(comments = listOf(CommentDto(1, "Anterior", 2))) }
        repository.getAlbum(100)
        repository.getAlbums()
        repository.addComment(100, NewComment("Nuevo", 5, 7))
        val album = repository.getAlbum(100)
        assertEquals(listOf("Anterior", "Nuevo"), album.comments.map { it.description })
        assertEquals(1, api.albumDetailCalls)
        assertFalse(repository.isCatalogCacheValid)
    }

    @Test fun `un envio fallido conserva el detalle y el catalogo en cache`() = runTest {
        val before = repository.getAlbum(100)
        repository.getAlbums()
        api.addCommentResponse = { _, _ -> throw IOException() }
        try {
            repository.addComment(100, NewComment("Nuevo", 4, 7))
            fail("Se esperaba un error de conexión")
        } catch (_: AppError.NoConnection) {
            assertEquals(before, repository.getAlbum(100))
            assertEquals(1, api.albumDetailCalls)
            assertTrue(repository.isCatalogCacheValid)
        }
    }
}
