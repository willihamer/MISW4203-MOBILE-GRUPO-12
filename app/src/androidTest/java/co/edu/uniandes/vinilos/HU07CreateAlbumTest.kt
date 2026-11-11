package co.edu.uniandes.vinilos

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.testutil.VinilosTestRule
import co.edu.uniandes.vinilos.testutil.awaitView
import co.edu.uniandes.vinilos.testutil.scrollToBottom
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matchers.containsString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/**
 * HU07 - Crear un álbum en modo coleccionista con respuestas HTTP controladas.
 */
@RunWith(AndroidJUnit4::class)
class HU07CreateAlbumTest {

    private val server = MockWebServer()

    @Volatile
    private var albumsJson = "[$INITIAL_ALBUM]"

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
                    request.path == "/albums" && request.method == "GET" -> json(albumsJson)
                    request.path == "/albums" && request.method == "POST" -> {
                        postCalls++
                        lastPostBody = request.body.readUtf8()
                        if (postStatus == 200) {
                            albumsJson = "[$INITIAL_ALBUM,$CREATED_ALBUM]"
                            json(CREATED_ALBUM).setResponseCode(200)
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

    private fun json(value: String) =
        MockResponse().setHeader("Content-Type", "application/json").setBody(value)

    private fun enterAsCollector() {
        onView(withText(R.string.welcome_collector_title)).perform(scrollTo(), click())
        awaitView(withText("Manolo Bellon"))
        onView(withText("Manolo Bellon")).perform(click())
        onView(withId(R.id.continue_button)).perform(click())
    }

    private fun fillForm(
        name: String = "Siembra",
        cover: String = "https://example.com/siembra.jpg",
        genre: String = "Salsa",
        recordLabel: String = "Elektra",
        description: String = "Álbum emblemático de la salsa publicado en 1978.",
    ) {
        onView(withId(R.id.name_input)).perform(scrollTo(), replaceText(name), closeSoftKeyboard())
        onView(withId(R.id.cover_input)).perform(scrollTo(), replaceText(cover), closeSoftKeyboard())

        onView(withId(R.id.release_date_input)).perform(scrollTo(), click())
        onView(withId(com.google.android.material.R.id.confirm_button)).perform(click())

        onView(withId(R.id.genre_input)).perform(scrollTo(), click())
        onView(withText(genre)).inRoot(isDialog()).perform(click())

        onView(withId(R.id.record_label_input)).perform(scrollTo(), click())
        onView(withText(recordLabel)).inRoot(isDialog()).perform(click())

        onView(withId(R.id.description_input)).perform(scrollTo(), replaceText(description), closeSoftKeyboard())
    }

    @Test
    fun opcionCrearAlbumSoloEstaDisponibleEnModoColeccionista() {
        onView(withText(R.string.welcome_visitor_title)).perform(click())
        awaitView(withText("Buscando América"))
        onView(withId(R.id.create_album_button)).check(matches(withEffectiveVisibility(Visibility.GONE)))

        onView(withId(R.id.profile_chip)).perform(click())
        enterAsCollector()
        onView(withId(R.id.create_album_button)).check(matches(isDisplayed()))
    }

    @Test
    fun muestraErroresDeValidacionCuandoLosCamposEstanVacios() {
        enterAsCollector()
        onView(withId(R.id.create_album_button)).perform(click())

        onView(withId(R.id.form_scroll)).perform(scrollToBottom())
        onView(withId(R.id.save_button)).perform(click())

        onView(withId(R.id.name_error))
            .check(matches(withText(R.string.album_create_error_required)))
        onView(withId(R.id.cover_error))
            .check(matches(withText(R.string.album_create_error_required)))
        onView(withId(R.id.release_date_error))
            .check(matches(withText(R.string.album_create_error_required)))
        onView(withId(R.id.genre_error))
            .check(matches(withText(R.string.album_create_error_required)))
        onView(withId(R.id.record_label_error))
            .check(matches(withText(R.string.album_create_error_required)))
        onView(withId(R.id.description_error))
            .check(matches(withText(R.string.album_create_error_required)))
        assertEquals(0, postCalls)
    }

    @Test
    fun validaFormatoDeUrlDePortadaYLimpiaErrorAlCorregirla() {
        enterAsCollector()
        onView(withId(R.id.create_album_button)).perform(click())

        fillForm(cover = "url-invalida")
        onView(withId(R.id.form_scroll)).perform(scrollToBottom())
        onView(withId(R.id.save_button)).perform(click())

        onView(withId(R.id.cover_error))
            .check(matches(withText(R.string.album_create_error_cover_url)))
        assertEquals(0, postCalls)

        onView(withId(R.id.cover_input))
            .perform(scrollTo(), replaceText("https://example.com/siembra.jpg"), closeSoftKeyboard())
        onView(withId(R.id.cover_error)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun creaUnAlbumExitosamenteConfirmaYApareceEnElCatalogo() {
        enterAsCollector()
        onView(withId(R.id.create_album_button)).perform(click())

        fillForm()
        onView(withId(R.id.preview_title)).perform(scrollTo()).check(matches(withText("Siembra")))
        onView(withId(R.id.preview_genre)).check(matches(withText("Salsa")))
        onView(withId(R.id.preview_record_label)).check(matches(withText(containsString("Elektra"))))

        onView(withId(R.id.form_scroll)).perform(scrollToBottom())
        onView(withId(R.id.save_button)).perform(click())

        awaitView(withText("Siembra"))
        onView(withText(R.string.album_create_success)).check(matches(isDisplayed()))
        onView(withText("Siembra")).check(matches(isDisplayed()))
        assertEquals(1, postCalls)
        assertTrue(lastPostBody.contains("\"name\":\"Siembra\""))
        assertTrue(lastPostBody.contains("\"genre\":\"Salsa\""))
        assertTrue(lastPostBody.contains("\"recordLabel\":\"Elektra\""))
    }

    @Test
    fun muestraErrorDelServidorAlGuardarYPermiteReintentar() {
        postStatus = 500
        enterAsCollector()
        onView(withId(R.id.create_album_button)).perform(click())

        fillForm()
        onView(withId(R.id.form_scroll)).perform(scrollToBottom())
        onView(withId(R.id.save_button)).perform(click())

        onView(withId(R.id.submit_error))
            .check(matches(withText(R.string.error_server)))

        postStatus = 200
        onView(withId(R.id.form_scroll)).perform(scrollToBottom())
        onView(withId(R.id.save_button)).perform(click())

        awaitView(withText("Siembra"))
        onView(withText("Siembra")).check(matches(isDisplayed()))
        assertEquals(2, postCalls)
    }

    @Test
    fun cancelarRegresaAlCatalogoSinCrearElAlbum() {
        enterAsCollector()
        onView(withId(R.id.create_album_button)).perform(click())

        onView(withId(R.id.name_input)).perform(replaceText("Borrador"), closeSoftKeyboard())
        onView(withId(R.id.form_scroll)).perform(scrollToBottom())
        onView(withId(R.id.cancel_button)).perform(click())

        onView(withId(R.id.album_list)).check(matches(isDisplayed()))
        assertEquals(0, postCalls)
    }

    companion object {
        private const val COLLECTORS =
            """[{"id":1,"name":"Manolo Bellon","email":"manolo@avana.com","telephone":"3502457896"}]"""
        private const val INITIAL_ALBUM =
            """{"id":100,"name":"Buscando América","cover":"https://example.com/buscando.jpg","releaseDate":"1984-08-01T00:00:00.000Z","description":"Primer álbum.","genre":"Salsa","recordLabel":"Elektra","performers":[],"tracks":[]}"""
        private const val CREATED_ALBUM =
            """{"id":200,"name":"Siembra","cover":"https://example.com/siembra.jpg","releaseDate":"1978-09-07T00:00:00.000Z","description":"Álbum emblemático de la salsa publicado en 1978.","genre":"Salsa","recordLabel":"Elektra","performers":[],"tracks":[]}"""
    }
}
