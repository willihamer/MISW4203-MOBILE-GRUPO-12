package co.edu.uniandes.vinilos

import android.view.View
import android.widget.RatingBar
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.testutil.VinilosTestRule
import co.edu.uniandes.vinilos.testutil.awaitView
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HU09AddCommentTest {
    private val server = MockWebServer()
    @Volatile private var postStatus = 200
    @Volatile private var postCalls = 0
    @Volatile private var lastPostBody = ""
    @Volatile private var albumDetail = ALBUM
    private val lifecycle = object : ExternalResource() {
        override fun before() {
            server.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse = when {
                    request.path == "/collectors" -> json("""[{"id":7,"name":"Ana","telephone":"123","email":"ana@example.com"}]""")
                    request.path == "/albums" -> json("[$ALBUM]")
                    request.path == "/albums/100" -> json(albumDetail)
                    request.path == "/albums/100/comments" && request.method == "POST" -> {
                        postCalls++
                        lastPostBody = request.body.readUtf8()
                        if (postStatus == 200) json("""{"id":400,"description":"Excelente álbum","rating":4}""")
                        else MockResponse().setResponseCode(postStatus)
                    }
                    else -> json("[]")
                }
            }
            server.start()
        }
        override fun after() { server.shutdown() }
    }
    private val app = VinilosTestRule { "http://localhost:${server.port}/" }
    @get:Rule val rules: RuleChain = RuleChain.outerRule(lifecycle).around(app)
    private fun json(body: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(body)

    private fun openAlbum(collector: Boolean = true) {
        if (collector) {
            onView(withText(R.string.welcome_collector_title)).perform(scrollTo(), click())
            awaitView(withText("Ana"), rootMatcher = isDialog())
            onView(withText("Ana")).inRoot(isDialog()).perform(click())
            onView(withId(R.id.continue_button)).perform(click())
        } else onView(withText(R.string.welcome_visitor_title)).perform(click())
        awaitView(withText("Álbum de prueba"))
        onView(withText("Álbum de prueba")).perform(click())
        awaitView(withId(R.id.album_name))
    }

    private fun openForm() {
        onView(withId(R.id.add_comment_button)).perform(scrollTo(), click())
        awaitView(withId(R.id.comment_input))
        androidx.test.espresso.Espresso.closeSoftKeyboard()
    }

    private fun fillForm() {
        onView(withId(R.id.comment_input)).perform(replaceText("Excelente álbum"), closeSoftKeyboard())
        onView(withId(R.id.comment_rating)).perform(scrollTo(), object : ViewAction {
            override fun getConstraints(): Matcher<View> = isAssignableFrom(RatingBar::class.java)
            override fun getDescription() = "Seleccionar cuatro estrellas con el teclado"
            override fun perform(uiController: UiController, view: View) {
                view.requestFocus()
                repeat(4) {
                    view.dispatchKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                    view.dispatchKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                }
                uiController.loopMainThreadUntilIdle()
            }
        })
    }

    @Test fun visitantePuedeLeerPeroNoPublicar() {
        openAlbum(collector = false)
        onView(withId(R.id.add_comment_button)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withText("Reseña anterior")).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test fun exigeLosDosCamposSinEnviarSolicitud() {
        openAlbum()
        openForm()
        onView(withId(R.id.save_comment_button)).perform(scrollTo(), click())
        onView(withId(R.id.comment_input)).check(matches(hasErrorText("Este campo es obligatorio")))
        onView(withId(R.id.rating_error)).check(matches(withText(R.string.comment_rating_required)))
        assertEquals(0, postCalls)
    }

    @Test fun publicaConElColeccionistaActivoYActualizaElDetalle() {
        openAlbum()
        openForm()
        onView(withId(R.id.comment_author)).check(matches(withText("Publicando como Ana (coleccionista)")))
        fillForm()
        onView(withId(R.id.save_comment_button)).perform(scrollTo(), click())
        val published = allOf(withId(R.id.comment_description), withText("Excelente álbum"))
        awaitView(published)
        onView(published).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText("4 de 5 estrellas")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText("Reseña anterior")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.comment_input)).check(doesNotExist())
        val body = JSONObject(lastPostBody)
        assertEquals(7, body.getJSONObject("collector").getInt("id"))
        assertEquals(4, body.getInt("rating"))
        assertEquals("Excelente álbum", body.getString("description"))
        assertEquals(1, postCalls)
    }

    @Test fun conservaElBorradorAnteErrorYPermiteReintentar() {
        postStatus = 500
        openAlbum()
        openForm()
        fillForm()
        onView(withId(R.id.save_comment_button)).perform(scrollTo(), click())
        onView(withId(R.id.submit_error)).check(matches(withText(R.string.error_server)))
        onView(withId(R.id.comment_input)).check(matches(withText("Excelente álbum")))
        postStatus = 200
        onView(withId(R.id.save_comment_button)).perform(scrollTo(), click())
        awaitView(allOf(withId(R.id.comment_description), withText("Excelente álbum")))
        onView(withId(R.id.comment_input)).check(doesNotExist())
        assertEquals(2, postCalls)
    }

    @Test fun cancelarNoPublica() {
        openAlbum()
        openForm()
        fillForm()
        onView(withId(R.id.cancel_button)).perform(scrollTo(), click())
        onView(withId(R.id.comment_input)).check(doesNotExist())
        assertEquals(0, postCalls)
    }

    @Test fun recrearLaActividadConservaElBorrador() {
        openAlbum()
        openForm()
        onView(withId(R.id.comment_rating)).perform(scrollTo(), click())
        onView(withId(R.id.rating_value)).check(matches(withText("3 de 5 estrellas")))
        val review = "Excelente álbum\nLo volvería a escuchar"
        onView(withId(R.id.comment_input)).perform(replaceText(review), closeSoftKeyboard())
        app.scenario.recreate()
        onView(withId(R.id.comment_input)).check(matches(withText(review)))
        onView(withId(R.id.rating_value)).check(matches(withText("3 de 5 estrellas")))
        onView(withId(R.id.comment_author)).check(matches(withText("Publicando como Ana (coleccionista)")))
        assertEquals(0, postCalls)
    }

    @Test fun albumSinComentariosMuestraEstadoVacio() {
        albumDetail = JSONObject(ALBUM).put("comments", JSONArray()).toString()
        openAlbum(collector = false)
        onView(withText(R.string.comments_empty)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.comment_description)).check(doesNotExist())
    }

    companion object {
        private const val ALBUM = """{"id":100,"name":"Álbum de prueba","cover":"","releaseDate":"2020-01-01","description":"Descripción","genre":"Rock","recordLabel":"EMI","performers":[],"tracks":[],"comments":[{"id":1,"description":"Reseña anterior","rating":3}]}"""
    }
}
