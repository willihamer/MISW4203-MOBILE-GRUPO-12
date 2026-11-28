package co.edu.uniandes.vinilos

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
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
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/**
 * HU11 - Agregar álbum a coleccionista (escenarios PE-HU11-01 a PE-HU11-08).
 */
@RunWith(AndroidJUnit4::class)
class HU11AddCollectorAlbumTest {
    private val server = MockWebServer()

    @Volatile private var postStatus = 200
    @Volatile private var postCalls = 0
    @Volatile private var lastPostBody = ""
    @Volatile private var collectedAlbumJson: String? = null

    private val lifecycle = object : ExternalResource() {
        override fun before() {
            server.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse = when {
                    request.path == "/collectors" && request.method == "GET" -> json("[$COLLECTOR]")
                    request.path == "/collectors/7" && request.method == "GET" -> json(COLLECTOR)
                    request.path == "/collectors/7/albums" && request.method == "GET" ->
                        json(collectedAlbumJson?.let { "[$it]" } ?: "[]")
                    request.path == "/collectors/7/albums/100" && request.method == "GET" ->
                        json(collectedAlbumJson?.let { "[$it]" } ?: "[]")
                    request.path == "/collectors/7/albums/100" && request.method == "POST" -> {
                        postCalls++
                        lastPostBody = request.body.readUtf8()
                        if (postStatus == 200) {
                            val req = JSONObject(lastPostBody)
                            val created = """{"id":500,"price":${req.getDouble("price")},"status":"${req.getString("status")}","album":$ALBUM}"""
                            collectedAlbumJson = created
                            json(created)
                        } else {
                            MockResponse().setResponseCode(postStatus)
                        }
                    }
                    request.path == "/albums" && request.method == "GET" -> json("[$ALBUM]")
                    request.path == "/albums/100" && request.method == "GET" -> json(ALBUM)
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

    private fun json(body: String) =
        MockResponse().setHeader("Content-Type", "application/json").setBody(body)

    private fun openAlbum(collector: Boolean = true) {
        if (collector) {
            onView(withText(R.string.welcome_collector_title)).perform(scrollTo(), click())
            awaitView(withText("Ana"), rootMatcher = isDialog())
            onView(withText("Ana")).inRoot(isDialog()).perform(click())
            onView(withId(R.id.continue_button)).perform(click())
        } else {
            onView(withText(R.string.welcome_visitor_title)).perform(click())
        }
        awaitView(withText("Álbum de prueba"))
        onView(withText("Álbum de prueba")).perform(click())
        awaitView(withId(R.id.album_name))
    }

    private fun openForm() {
        onView(withId(R.id.add_to_collection_button)).perform(scrollTo(), click())
        awaitView(withId(R.id.price_input))
        androidx.test.espresso.Espresso.closeSoftKeyboard()
    }

    private fun fillForm(price: String = "85000", active: Boolean = true) {
        onView(withId(R.id.price_input)).perform(replaceText(price), closeSoftKeyboard())
        val statusButtonId = if (active) R.id.status_active else R.id.status_inactive
        onView(withId(statusButtonId)).perform(scrollTo(), click())
    }

    // PE-HU11-01
    @Test
    fun visitanteNoVeLaOpcion() {
        openAlbum(collector = false)
        onView(withId(R.id.add_to_collection_button))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // PE-HU11-02
    @Test
    fun exigePrecioYEstadoSinEnviarSolicitud() {
        openAlbum()
        openForm()

        onView(withId(R.id.save_collection_button)).perform(scrollTo(), click())

        onView(withId(R.id.price_input)).check(matches(hasErrorText("Este campo es obligatorio")))
        onView(withId(R.id.status_error)).check(matches(withText(R.string.collection_status_required)))
        assertEquals(0, postCalls)
    }

    // PE-HU11-03
    @Test
    fun rechazaPrecioNoPositivo() {
        openAlbum()
        openForm()

        fillForm(price = "0", active = true)
        onView(withId(R.id.save_collection_button)).perform(scrollTo(), click())

        onView(withId(R.id.price_input)).check(matches(hasErrorText("Ingresa un precio mayor que cero")))
        assertEquals(0, postCalls)
    }

    // PE-HU11-04
    @Test
    fun agregaYApareceEnLaColeccion() {
        openAlbum()
        openForm()
        fillForm(price = "85000", active = true)

        onView(withId(R.id.save_collection_button)).perform(scrollTo(), click())

        awaitView(withText(R.string.collection_success))
        onView(withId(R.id.price_input)).check(doesNotExist())

        val body = JSONObject(lastPostBody)
        assertEquals(85000.0, body.getDouble("price"), 0.0)
        assertEquals("Active", body.getString("status"))
        assertEquals(1, postCalls)

        // Navega al detalle del coleccionista (HU06) y comprueba que el álbum está en su colección
        onView(withId(R.id.close_button)).perform(click())
        onView(withId(R.id.tab_collectors)).perform(click())
        // "Ana" también es el nombre del chip de perfil: se busca solo en la lista de coleccionistas
        val anaInList = allOf(withText("Ana"), isDescendantOfA(withId(R.id.collector_list)))
        awaitView(anaInList)
        onView(anaInList).perform(click())
        awaitView(withId(R.id.collector_name))
        onView(withId(R.id.collection_album_list))
            .check(matches(hasDescendant(withText("Álbum de prueba"))))
    }

    // PE-HU11-05
    @Test
    fun informaSiYaEstaEnLaColeccion() {
        collectedAlbumJson = """{"id":500,"price":85000,"status":"Active","album":$ALBUM}"""
        openAlbum()
        openForm()
        fillForm(price = "90000", active = false)

        onView(withId(R.id.save_collection_button)).perform(scrollTo(), click())

        onView(withId(R.id.submit_error)).check(matches(withText(R.string.collection_already_added)))
        onView(withId(R.id.save_collection_button)).check(matches(not(isEnabled())))
        assertEquals(0, postCalls)
    }

    // PE-HU11-06
    @Test
    fun conservaElBorradorAnteErrorYPermiteReintentar() {
        postStatus = 500
        openAlbum()
        openForm()
        fillForm(price = "85000", active = false)

        onView(withId(R.id.save_collection_button)).perform(scrollTo(), click())

        onView(withId(R.id.submit_error)).check(matches(withText(R.string.error_server)))
        onView(withId(R.id.price_input)).check(matches(withText("85000")))
        onView(withId(R.id.status_inactive)).check(matches(isChecked()))

        postStatus = 200
        onView(withId(R.id.save_collection_button)).perform(scrollTo(), click())

        awaitView(withText(R.string.collection_success))
        onView(withId(R.id.price_input)).check(doesNotExist())
        assertEquals(2, postCalls)
    }

    // PE-HU11-07
    @Test
    fun cancelarNoAgrega() {
        openAlbum()
        openForm()
        fillForm(price = "85000", active = true)

        onView(withId(R.id.cancel_button)).perform(scrollTo(), click())

        onView(withId(R.id.price_input)).check(doesNotExist())
        assertEquals(0, postCalls)
    }

    // PE-HU11-08
    @Test
    fun recrearLaActividadConservaElBorrador() {
        openAlbum()
        openForm()
        fillForm(price = "120000", active = false)

        app.scenario.recreate()

        onView(withId(R.id.price_input)).check(matches(withText("120000")))
        onView(withId(R.id.status_inactive)).check(matches(isChecked()))
        assertEquals(0, postCalls)
    }

    companion object {
        private const val COLLECTOR =
            """{"id":7,"name":"Ana","telephone":"123","email":"ana@example.com","favoritePerformers":[]}"""
        private const val ALBUM =
            """{"id":100,"name":"Álbum de prueba","cover":"","releaseDate":"2020-01-01","description":"Descripción","genre":"Rock","recordLabel":"EMI","performers":[{"id":101,"name":"Queen","image":"","creationDate":"1970-01-01"}],"tracks":[],"comments":[]}"""
    }
}
