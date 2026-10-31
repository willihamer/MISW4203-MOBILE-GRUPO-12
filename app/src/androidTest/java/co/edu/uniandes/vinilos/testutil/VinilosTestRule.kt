package co.edu.uniandes.vinilos.testutil

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.platform.app.InstrumentationRegistry
import co.edu.uniandes.vinilos.MainActivity
import co.edu.uniandes.vinilos.VinilosApplication
import co.edu.uniandes.vinilos.data.network.NetworkIdlingResource
import org.junit.rules.ExternalResource

/**
 * Arranca cada prueba desde P01 (sin perfil guardado) y hace que Espresso espere a la red
 * con el idling resource del Service Adapter.
 */
class VinilosTestRule(private val baseUrl: (() -> String)? = null) : ExternalResource() {

    lateinit var scenario: ActivityScenario<MainActivity>
        private set

    override fun before() {
        // La Application sobrevive entre pruebas: se limpia el perfil en el repositorio, no solo en disco.
        val app = ApplicationProvider.getApplicationContext<VinilosApplication>()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            (app as TestVinilosApplication).configure(baseUrl?.invoke() ?: co.edu.uniandes.vinilos.BuildConfig.BASE_URL)
            app.container.profileRepository.clear()
        }
        IdlingRegistry.getInstance().register(NetworkIdlingResource.resource)
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    override fun after() {
        scenario.close()
        IdlingRegistry.getInstance().unregister(NetworkIdlingResource.resource)
    }
}
