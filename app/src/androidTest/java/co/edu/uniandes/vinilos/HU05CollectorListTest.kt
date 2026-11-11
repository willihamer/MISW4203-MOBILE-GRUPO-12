package co.edu.uniandes.vinilos

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.data.network.NetworkIdlingResource
import co.edu.uniandes.vinilos.testutil.VinilosTestRule
import co.edu.uniandes.vinilos.testutil.awaitView
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** HU05: listado, búsqueda y selección con respuestas HTTP controladas. */
@RunWith(AndroidJUnit4::class)
class HU05CollectorListTest {
    private val server = MockWebServer()
    @Volatile private var body = COLLECTORS
    @Volatile private var status = 200
    @Volatile private var gate: CountDownLatch? = null
    private val lifecycle = object : ExternalResource() {
        override fun before() {
            server.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    if (request.path != "/collectors") return json("[]")
                    if (gate?.await(15, TimeUnit.SECONDS) == false) return MockResponse().setResponseCode(504)
                    return json(body).setResponseCode(status)
                }
            }
            server.start()
        }
        override fun after() { server.shutdown() }
    }
    private val app = VinilosTestRule { "http://localhost:${server.port}/" }
    @get:Rule val rules: RuleChain = RuleChain.outerRule(lifecycle).around(app)
    private fun json(value: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(value)
    private fun enter() {
        onView(withText(R.string.welcome_visitor_title)).perform(click())
        onView(withId(R.id.tab_collectors)).perform(click())
    }
    private fun search(value: String) {
        onView(withId(R.id.search_field)).perform(replaceText(value), closeSoftKeyboard())
    }
    @Test fun muestraNombresYCorreosDelListado() {
        enter()
        awaitView(withText("José Pérez"))
        onView(withText("jose@example.com")).check(matches(isDisplayed()))
        onView(withText("Ana López")).check(matches(isDisplayed()))
        onView(withText("ana@example.com")).check(matches(isDisplayed()))
    }
    @Test fun buscaSinTildesYRecuperaElListadoAlLimpiar() {
        enter()
        search("JOSE")
        awaitView(withText("Ana López"), present = false)
        onView(withText("José Pérez")).check(matches(isDisplayed()))
        search("")
        awaitView(withText("Ana López"))
        onView(withText("Ana López")).check(matches(isDisplayed()))
    }
    @Test fun informaBusquedaSinResultadosYPermiteOtraConsulta() {
        enter()
        search("inexistente")
        onView(withText(R.string.collectors_no_results_title)).check(matches(isDisplayed()))
        search("ana")
        awaitView(withText("Ana López"))
        onView(withText("Ana López")).check(matches(isDisplayed()))
    }
    @Test fun abreElColeccionistaSeleccionadoYConservaBusquedaAlVolver() {
        enter()
        search("ana")
        awaitView(withText("Ana López"))
        onView(withText("Ana López")).perform(click())
        onView(withId(R.id.collector_name)).check(matches(withText("Ana López")))
        onView(withId(R.id.collector_email)).check(matches(withText("ana@example.com")))
        app.scenario.recreate()
        onView(withId(R.id.collector_name)).check(matches(withText("Ana López")))
        onView(withId(R.id.back_button)).perform(click())
        onView(withId(R.id.search_field)).check(matches(withText("ana")))
        onView(withText("Ana López")).check(matches(isDisplayed()))
    }
    @Test fun conservaLaBusquedaAlRecrearLaPantalla() {
        enter()
        search("jose")
        app.scenario.recreate()
        onView(withId(R.id.search_field)).check(matches(withText("jose")))
        awaitView(withText("Ana López"), present = false)
        onView(withText("José Pérez")).check(matches(isDisplayed()))
    }
    @Test fun muestraEstadoVacioCuandoNoHayColeccionistas() {
        body = "[]"
        enter()
        onView(withText(R.string.collectors_empty_title)).check(matches(isDisplayed()))
    }
    @Test fun permiteReintentarDespuesDeUnErrorDelServidor() {
        status = 500
        enter()
        onView(withText(R.string.error_server)).check(matches(isDisplayed()))
        status = 200
        onView(withId(R.id.retry_button)).perform(scrollTo(), click())
        awaitView(withText("José Pérez"))
        onView(withText("José Pérez")).check(matches(isDisplayed()))
    }
    @Test fun muestraCargaMientrasEsperaLaRespuesta() {
        val release = CountDownLatch(1)
        gate = release
        IdlingRegistry.getInstance().unregister(NetworkIdlingResource.resource)
        try {
            enter()
            onView(withText(R.string.collectors_loading)).check(matches(isDisplayed()))
        } finally {
            release.countDown()
            IdlingRegistry.getInstance().register(NetworkIdlingResource.resource)
        }
        awaitView(withText("José Pérez"))
        onView(withText("José Pérez")).check(matches(isDisplayed()))
    }
    companion object {
        private const val COLLECTORS = """[{"id":7,"name":"José Pérez","email":"jose@example.com","telephone":"123"},{"id":42,"name":"Ana López","email":"ana@example.com","telephone":"456"}]"""
    }
}
