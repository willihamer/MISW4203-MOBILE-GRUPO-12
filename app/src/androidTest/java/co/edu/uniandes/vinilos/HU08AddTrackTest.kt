package co.edu.uniandes.vinilos

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.testutil.VinilosTestRule
import co.edu.uniandes.vinilos.testutil.awaitView
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/**
 * HU08 - Asociar tracks a un álbum en modo coleccionista. Las escrituras van contra
 * MockWebServer para no crear datos en el backend compartido.
 */
@RunWith(AndroidJUnit4::class)
class HU08AddTrackTest {

    private val server = MockWebServer()

    @Volatile
    private var tracksJson = "[]"

    @Volatile
    private var postStatus = 200

    @Volatile
    private var postCalls = 0

    @Volatile
    private var lastPostBody = ""

    private val lifecycle = object : ExternalResource() {
        override fun before() {
            server.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse = when {
                    request.path == "/collectors" && request.method == "GET" -> json(COLLECTORS)
                    request.path == "/albums" && request.method == "GET" -> json("[${album()}]")
                    request.path == "/albums/100" && request.method == "GET" -> json(album())
                    request.path == "/albums/100/tracks" && request.method == "POST" -> {
                        postCalls++
                        lastPostBody = request.body.readUtf8()
                        if (postStatus == 200) {
                            tracksJson = "[$CREATED_TRACK]"
                            json(CREATED_TRACK)
                        } else {
                            MockResponse().setResponseCode(postStatus)
                        }
                    }
                    else -> json("[]")
                }
            }
            server.start()
        }

        override fun after() {
            server.shutdown()
        }
    }

    private val app = VinilosTestRule { "http://localhost:${server.port}/" }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(lifecycle).around(app)

    private fun album() =
        """{"id":100,"name":"Buscando América","cover":"https://example.com/buscando.jpg","releaseDate":"1984-08-01T00:00:00.000Z","description":"Primer álbum.","genre":"Salsa","recordLabel":"Elektra","performers":[{"id":100,"name":"Rubén Blades","image":"","birthDate":"1948-07-16T00:00:00.000Z"}],"tracks":$tracksJson}"""

    private fun json(value: String) =
        MockResponse().setHeader("Content-Type", "application/json").setBody(value)

    private fun enterAsCollectorAndOpenAlbum() {
        onView(withText(R.string.welcome_collector_title)).perform(scrollTo(), click())
        awaitView(withText("Manolo Bellon"))
        onView(withText("Manolo Bellon")).perform(click())
        onView(withId(R.id.continue_button)).perform(click())
        openAlbum()
    }

    private fun openAlbum() {
        awaitView(withText("Buscando América"))
        onView(withText("Buscando América")).perform(click())
        awaitView(withId(R.id.album_name))
    }

    private fun openAddTrackForm() {
        onView(withId(R.id.add_track_button)).perform(scrollTo(), click())
        awaitView(withId(R.id.track_name_input))
        // En Android 5 el teclado puede abrirse solo y tapar los botones de la hoja
        androidx.test.espresso.Espresso.closeSoftKeyboard()
    }

    private fun fillForm(name: String, duration: String) {
        onView(withId(R.id.track_name_input)).perform(replaceText(name), closeSoftKeyboard())
        onView(withId(R.id.track_duration_input)).perform(replaceText(duration), closeSoftKeyboard())
    }

    // PE-HU08-01
    @Test
    fun opcionAgregarTrackSoloEstaDisponibleEnModoColeccionista() {
        onView(withText(R.string.welcome_visitor_title)).perform(click())
        openAlbum()
        onView(withId(R.id.add_track_button)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // PE-HU08-01
    @Test
    fun elColeccionistaVeLaOpcionYElFormularioConElAlbumDeDestino() {
        enterAsCollectorAndOpenAlbum()
        openAddTrackForm()

        onView(withId(R.id.album_subtitle)).check(matches(withText("Buscando América • Rubén Blades")))
        onView(withId(R.id.track_duration_helper)).check(matches(withText(R.string.track_add_duration_helper)))
    }

    // PE-HU08-02
    @Test
    fun muestraErroresCuandoLosCamposEstanVacios() {
        enterAsCollectorAndOpenAlbum()
        openAddTrackForm()

        onView(withId(R.id.save_track_button)).perform(scrollTo(), click())

        onView(withId(R.id.track_name_error)).check(matches(withText(R.string.error_required_field)))
        onView(withId(R.id.track_duration_error)).check(matches(withText(R.string.error_required_field)))
        assertEquals(0, postCalls)
    }

    // PE-HU08-03
    @Test
    fun validaElFormatoMmSsDeLaDuracion() {
        enterAsCollectorAndOpenAlbum()
        openAddTrackForm()

        listOf("5:5", "03:75", "0:00").forEach { duration ->
            fillForm(name = "Decisiones", duration = duration)
            onView(withId(R.id.save_track_button)).perform(scrollTo(), click())
            onView(withId(R.id.track_duration_error))
                .check(matches(withText(R.string.track_add_error_duration_format)))
        }
        assertEquals(0, postCalls)

        onView(withId(R.id.track_duration_input)).perform(replaceText("05:05"), closeSoftKeyboard())
        onView(withId(R.id.track_duration_error)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // PE-HU08-04
    @Test
    fun agregaUnTrackYApareceEnElDetalle() {
        enterAsCollectorAndOpenAlbum()
        onView(withId(R.id.tracks_empty)).perform(scrollTo()).check(matches(isDisplayed()))
        openAddTrackForm()

        fillForm(name = "Decisiones", duration = "5:05")
        onView(withId(R.id.save_track_button)).perform(scrollTo(), click())

        awaitView(withText("Decisiones · 5:05"))
        onView(withText("Decisiones · 5:05")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.tracks_empty)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.track_name_input)).check(doesNotExist())
        assertEquals(1, postCalls)
        assertTrue(lastPostBody.contains("\"name\":\"Decisiones\""))
        assertTrue(lastPostBody.contains("\"duration\":\"5:05\""))
    }

    // PE-HU08-05
    @Test
    fun muestraErrorDelServidorYPermiteReintentar() {
        postStatus = 500
        enterAsCollectorAndOpenAlbum()
        openAddTrackForm()

        fillForm(name = "Decisiones", duration = "5:05")
        onView(withId(R.id.save_track_button)).perform(scrollTo(), click())
        onView(withId(R.id.submit_error)).check(matches(withText(R.string.error_server)))

        postStatus = 200
        onView(withId(R.id.save_track_button)).perform(scrollTo(), click())

        awaitView(withText("Decisiones · 5:05"))
        assertEquals(2, postCalls)
    }

    // PE-HU08-06
    @Test
    fun cancelarRegresaAlDetalleSinCrearElTrack() {
        enterAsCollectorAndOpenAlbum()
        openAddTrackForm()

        fillForm(name = "Borrador", duration = "3:00")
        onView(withId(R.id.cancel_button)).perform(scrollTo(), click())

        onView(withId(R.id.track_name_input)).check(doesNotExist())
        onView(withId(R.id.album_name)).check(matches(withText("Buscando América")))
        onView(withId(R.id.tracks_empty)).perform(scrollTo()).check(matches(isDisplayed()))
        assertEquals(0, postCalls)
    }

    companion object {
        private const val COLLECTORS =
            """[{"id":100,"name":"Manolo Bellon","email":"manollo@caracol.com.co","telephone":"3502457896"}]"""
        private const val CREATED_TRACK = """{"id":300,"name":"Decisiones","duration":"5:05"}"""
    }
}
