package co.edu.uniandes.vinilos

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.data.network.NetworkIdlingResource
import co.edu.uniandes.vinilos.testutil.awaitView
import co.edu.uniandes.vinilos.testutil.VinilosTestRule
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/** Escenarios negativos de la estrategia: HTTP real a servidor controlado en el dispositivo. */
@RunWith(AndroidJUnit4::class)
class AlbumErrorFlowTest {
    private val server = MockWebServer()
    private val lifecycle = object : ExternalResource() {
        override fun before() { server.start() }
        override fun after() { server.shutdown() }
    }
    private val app = VinilosTestRule { "http://localhost:${server.port}/" }
    @get:Rule val rules: RuleChain = RuleChain.outerRule(lifecycle).around(app)
    private fun json(body: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(body)
    private fun enter() { onView(withText(R.string.welcome_visitor_title)).perform(click()) }

    // PE-HU01-03: controlar la respuesta permite observar carga sin sleep.
    @Test fun muestraCargaMientrasEsperaLaRespuesta() {
        val release = java.util.concurrent.CountDownLatch(1)
        server.dispatcher = object : okhttp3.mockwebserver.Dispatcher() {
            override fun dispatch(request: okhttp3.mockwebserver.RecordedRequest): MockResponse {
                return if (release.await(15, TimeUnit.SECONDS)) json("[$ALBUM]")
                else MockResponse().setResponseCode(504)
            }
        }
        IdlingRegistry.getInstance().unregister(NetworkIdlingResource.resource)
        try {
            enter()
            onView(withText(R.string.albums_loading)).check(matches(isDisplayed()))
        } finally {
            release.countDown()
            IdlingRegistry.getInstance().register(NetworkIdlingResource.resource)
        }
        awaitView(withText("Álbum de prueba"))
        onView(withText("Álbum de prueba")).check(matches(isDisplayed()))
    }
    @Test fun catalogoError500PermiteReintentar() {
        server.enqueue(MockResponse().setResponseCode(500))
        enter()
        onView(withText(R.string.error_server)).check(matches(isDisplayed()))
        server.enqueue(json("[$ALBUM]"))
        onView(withId(R.id.retry_button)).perform(click())
        awaitView(withText("Álbum de prueba"))
        onView(withText("Álbum de prueba")).check(matches(isDisplayed()))
    }
    @Test fun catalogoVacioMuestraEstadoVacio() {
        server.enqueue(json("[]"))
        enter()
        onView(withText(R.string.albums_empty_title)).check(matches(isDisplayed()))
    }
    @Test fun detalleErrorPermiteReintentar() {
        server.enqueue(json("[$ALBUM]"))
        server.enqueue(MockResponse().setResponseCode(500))
        enter()
        onView(withText("Álbum de prueba")).perform(click())
        onView(withId(R.id.error_message)).check(matches(withText(R.string.error_server)))
        server.enqueue(json(ALBUM))
        onView(withId(R.id.retry_button)).perform(click())
        onView(withId(R.id.album_name)).check(matches(withText("Álbum de prueba")))
    }
    @Test fun detalle404MuestraMensajeSinCerrarLaApp() {
        server.enqueue(json("[$ALBUM]"))
        server.enqueue(MockResponse().setResponseCode(404))
        enter()
        onView(withText("Álbum de prueba")).perform(click())
        onView(withId(R.id.error_message)).check(matches(withText(R.string.error_album_not_found)))
        onView(withId(R.id.close_button)).perform(click())
        onView(withId(R.id.album_list)).check(matches(isDisplayed()))
    }
    companion object {
        private const val ALBUM = """{"id":100,"name":"Álbum de prueba","cover":"","releaseDate":"1984-08-01T00:00:00Z","description":"Descripción de prueba","genre":"Salsa","recordLabel":"Elektra","performers":[],"tracks":[]}"""
    }
}
