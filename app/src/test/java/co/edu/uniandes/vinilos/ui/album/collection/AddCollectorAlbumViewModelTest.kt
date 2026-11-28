package co.edu.uniandes.vinilos.ui.album.collection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.CollectorAlbumStatus
import co.edu.uniandes.vinilos.data.model.Profile
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.data.network.dto.CollectorAlbumDto
import co.edu.uniandes.vinilos.data.repository.CollectorRepositoryImpl
import co.edu.uniandes.vinilos.data.repository.ProfileRepository
import co.edu.uniandes.vinilos.testutil.FakeVinilosApi
import co.edu.uniandes.vinilos.testutil.MainDispatcherRule
import co.edu.uniandes.vinilos.testutil.SampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class AddCollectorAlbumViewModelTest {
    @get:Rule val instantTaskRule = InstantTaskExecutorRule()
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeVinilosApi()
    private val repository = CollectorRepositoryImpl(NetworkServiceAdapter(api), mainDispatcherRule.dispatcher)
    private val profiles = object : ProfileRepository {
        override val profile = MutableLiveData<Profile?>(Profile.CollectorProfile(7, "José Pérez"))
        override val current: Profile? get() = profile.value
        override fun selectVisitor() { profile.value = Profile.Visitor }
        override fun selectCollector(collectorId: Int, name: String) {
            profile.value = Profile.CollectorProfile(collectorId, name)
        }
        override fun clear() { profile.value = null }
    }

    private fun model(state: SavedStateHandle = SavedStateHandle(mapOf("albumId" to 100))) =
        AddCollectorAlbumViewModel(repository, profiles, state)

    @Test
    fun `exige precio y estado cuando ambos estan vacios sin enviar solicitud`() {
        val vm = model()
        vm.onPriceChanged("   ")
        vm.submit()

        assertEquals(R.string.error_required_field, vm.formState.value?.priceError)
        assertEquals(R.string.collection_status_required, vm.formState.value?.statusError)
        assertEquals(0, api.addCollectorAlbumCalls)
    }

    @Test
    fun `rechaza precios no numericos cero o negativos sin enviar solicitud`() {
        val vm = model()
        vm.onStatusChanged(CollectorAlbumStatus.ACTIVE)

        listOf("0", "0.00", "0,0", "-5", "-0.01", "abc", "10.5.2", "NaN", "100d").forEach { invalid ->
            vm.onPriceChanged(invalid)
            vm.submit()
            assertEquals("Precio inválido no detectado: $invalid", R.string.collection_price_invalid, vm.formState.value?.priceError)
            assertNull(vm.formState.value?.statusError)
        }
        assertEquals(0, api.addCollectorAlbumCalls)
    }

    @Test
    fun `envia el album al coleccionista activo con precios enteros o decimales y ambos estados`() {
        val cases = listOf(
            Triple("85000", CollectorAlbumStatus.ACTIVE, 85000.0 to "Active"),
            Triple("85000,50", CollectorAlbumStatus.INACTIVE, 85000.5 to "Inactive"),
            Triple("0.01", CollectorAlbumStatus.ACTIVE, 0.01 to "Active"),
        )
        cases.forEachIndexed { index, (rawPrice, status, expected) ->
            val localApi = FakeVinilosApi()
            val localRepo = CollectorRepositoryImpl(NetworkServiceAdapter(localApi), mainDispatcherRule.dispatcher)
            val vm = AddCollectorAlbumViewModel(localRepo, profiles, SavedStateHandle(mapOf("albumId" to 100)))

            vm.onPriceChanged("  $rawPrice  ")
            vm.onStatusChanged(status)
            vm.submit()

            assertEquals("Caso $index", SaveCollectorAlbumUiState.Success, vm.saveState.value)
            assertEquals(7, localApi.lastCollectorAlbumCollectorId)
            assertEquals(100, localApi.lastCollectorAlbumAlbumId)
            assertEquals(expected.first, localApi.lastCollectorAlbumRequest?.price ?: 0.0, 0.0)
            assertEquals(expected.second, localApi.lastCollectorAlbumRequest?.status)
        }
    }

    @Test
    fun `visitante o perfil sin identificador valido no puede agregar a la coleccion`() {
        val vm = model()
        vm.onPriceChanged("85000")
        vm.onStatusChanged(CollectorAlbumStatus.ACTIVE)

        profiles.selectVisitor()
        vm.submit()
        assertEquals(SaveCollectorAlbumUiState.Error(R.string.collection_collector_required), vm.saveState.value)

        profiles.clear()
        vm.submit()
        assertEquals(SaveCollectorAlbumUiState.Error(R.string.collection_collector_required), vm.saveState.value)

        profiles.selectCollector(0, "Inválido")
        vm.submit()
        assertEquals(SaveCollectorAlbumUiState.Error(R.string.collection_collector_required), vm.saveState.value)
        assertEquals(0, api.addCollectorAlbumCalls)
    }

    @Test
    fun `si el album ya esta en la coleccion informa AlreadyInCollection y no hace POST`() {
        profiles.selectCollector(100, "Manolo Bellon")
        val vm = model()
        vm.onPriceChanged("85000")
        vm.onStatusChanged(CollectorAlbumStatus.ACTIVE)

        vm.submit()

        assertEquals(SaveCollectorAlbumUiState.AlreadyInCollection, vm.saveState.value)
        assertEquals(0, api.addCollectorAlbumCalls)
    }

    @Test
    fun `no duplica envios mientras guarda ni despues de exito o duplicado`() {
        val vm = model()
        vm.onPriceChanged("85000")
        vm.onStatusChanged(CollectorAlbumStatus.ACTIVE)
        api.addCollectorAlbumResponse = { _, _, req ->
            vm.submit()
            CollectorAlbumDto(id = 500, price = req.price, status = req.status, album = SampleData.buscandoAmerica)
        }

        vm.submit()
        vm.submit()

        assertEquals(1, api.addCollectorAlbumCalls)
    }

    @Test
    fun `un fallo de red conserva el borrador y permite reintentar`() {
        val vm = model()
        vm.onPriceChanged("85000")
        vm.onStatusChanged(CollectorAlbumStatus.INACTIVE)
        api.addCollectorAlbumResponse = { _, _, _ -> throw IOException() }

        vm.submit()

        assertEquals(SaveCollectorAlbumUiState.Error(R.string.error_no_connection), vm.saveState.value)
        assertEquals("85000", vm.formState.value?.price)
        assertEquals(CollectorAlbumStatus.INACTIVE, vm.formState.value?.status)

        api.addCollectorAlbumResponse = FakeVinilosApi().addCollectorAlbumResponse
        vm.submit()

        assertEquals(SaveCollectorAlbumUiState.Success, vm.saveState.value)
        assertEquals(2, api.addCollectorAlbumCalls)
    }

    @Test
    fun `restaura el borrador desde SavedStateHandle`() {
        val state = SavedStateHandle(mapOf("albumId" to 100))
        val vm = model(state)
        vm.onPriceChanged("120000")
        vm.onStatusChanged(CollectorAlbumStatus.INACTIVE)

        val restored = model(state)

        assertEquals("120000", restored.formState.value?.price)
        assertEquals(CollectorAlbumStatus.INACTIVE, restored.formState.value?.status)
    }
}
