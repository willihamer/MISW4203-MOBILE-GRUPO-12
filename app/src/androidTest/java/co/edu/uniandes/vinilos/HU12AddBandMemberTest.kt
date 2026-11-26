package co.edu.uniandes.vinilos

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import co.edu.uniandes.vinilos.testutil.VinilosTestRule
import co.edu.uniandes.vinilos.testutil.awaitView
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.util.Collections

/**
 * HU12 - Agregar músicos a una banda (escenarios PE-HU12-01 a PE-HU12-08).
 *
 * Usa MockWebServer para no modificar el backend compartido. La banda Queen (id 101) empieza
 * con Freddie Mercury como integrante, salvo que la prueba cambie [memberIds]; cada POST agrega
 * el músico a esa lista, como hace BackVynils.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class HU12AddBandMemberTest {

    private val server = MockWebServer()

    private val memberIds: MutableList<Int> = Collections.synchronizedList(mutableListOf(FREDDIE_ID))
    private val postPaths: MutableList<String> = Collections.synchronizedList(mutableListOf())

    @Volatile
    private var postStatus = 200

    private val lifecycle = object : ExternalResource() {
        override fun before() {
            server.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path.orEmpty()
                    return when {
                        request.method == "POST" && MEMBER_POST.matches(path) -> addMember(path)
                        path == "/collectors" -> json(COLLECTORS)
                        path == "/musicians" -> json("[${MUSICIANS.values.joinToString(",")}]")
                        path == "/bands" -> json("[${band()}]")
                        path == "/bands/101" -> json(band())
                        else -> json("[]")
                    }
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

    private fun addMember(path: String): MockResponse {
        postPaths += path
        if (postStatus != 200) return MockResponse().setResponseCode(postStatus)
        val musicianId = MEMBER_POST.matchEntire(path)!!.groupValues[1].toInt()
        if (musicianId !in memberIds) memberIds += musicianId
        return json(MUSICIANS.getValue(musicianId))
    }

    private fun band() =
        """{"id":101,"name":"Queen","image":"","description":"Banda británica de rock.","creationDate":"1970-01-01T00:00:00.000Z","albums":[],"musicians":[${memberIds.joinToString(",") { MUSICIANS.getValue(it) }}]}"""

    private fun json(value: String) =
        MockResponse().setHeader("Content-Type", "application/json").setBody(value)

    private fun enterAsCollector() {
        onView(withText(R.string.welcome_collector_title)).perform(scrollTo(), click())
        awaitView(withText("Manolo Bellon"))
        onView(withText("Manolo Bellon")).perform(click())
        onView(withId(R.id.continue_button)).perform(click())
    }

    private fun openQueen() {
        onView(withId(R.id.tab_artists)).perform(click())
        awaitView(withText("Queen"))
        onView(withText("Queen")).perform(click())
        awaitView(withId(R.id.members_section))
    }

    private fun openAddMember() {
        onView(withId(R.id.add_member_button)).perform(scrollTo(), click())
        awaitView(withId(R.id.musician_list))
    }

    private fun selectMusician(name: String) {
        onView(allOf(isDescendantOfA(withId(R.id.musician_list)), withText(name))).perform(click())
    }

    private fun inMembers(name: String) = allOf(isDescendantOfA(withId(R.id.member_list)), withText(name))

    // PE-HU12-01
    @Test
    fun opcionAgregarMusicoSoloEstaDisponibleEnModoColeccionista() {
        onView(withText(R.string.welcome_visitor_title)).perform(click())
        openQueen()

        onView(withId(R.id.add_member_button)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // PE-HU12-02
    @Test
    fun laListaExcluyeALosIntegrantesActualesDeLaBanda() {
        enterAsCollector()
        openQueen()
        openAddMember()

        onView(withId(R.id.band_name)).check(matches(withText("Queen")))
        onView(withId(R.id.musician_list)).check(matches(hasDescendant(withText("Rubén Blades Bellido de Luna"))))
        onView(withId(R.id.musician_list)).check(matches(hasDescendant(withText("Willie Colón"))))
        onView(withId(R.id.musician_list)).check(matches(not(hasDescendant(withText("Freddie Mercury")))))
        onView(withId(R.id.add_members_button)).check(matches(not(isEnabled())))
    }

    // PE-HU12-03
    @Test
    fun buscaSinTildesYAvisaCuandoNingunMusicoCoincide() {
        enterAsCollector()
        openQueen()
        openAddMember()

        onView(withId(R.id.search_field)).perform(replaceText("RUBEN"), closeSoftKeyboard())
        awaitView(withText("Willie Colón"), present = false)
        onView(withText("Rubén Blades Bellido de Luna")).check(matches(isDisplayed()))

        onView(withId(R.id.search_field)).perform(replaceText("zzz"), closeSoftKeyboard())
        onView(withId(R.id.message)).check(matches(withText(R.string.band_member_no_results)))
    }

    // PE-HU12-04
    @Test
    fun agregaUnMusicoYApareceEntreLosIntegrantes() {
        enterAsCollector()
        openQueen()
        openAddMember()

        selectMusician("Willie Colón")
        onView(withId(R.id.add_members_button)).check(matches(isEnabled())).perform(click())

        awaitView(inMembers("Willie Colón"))
        onView(withText("Se agregó 1 músico a la banda")).check(matches(isDisplayed()))
        onView(withId(R.id.musician_list)).check(doesNotExist())
        assertEquals(listOf("/bands/101/musicians/102"), postPaths.toList())
    }

    // PE-HU12-05
    @Test
    fun agregaVariosMusicosALaVez() {
        enterAsCollector()
        openQueen()
        openAddMember()

        selectMusician("Rubén Blades Bellido de Luna")
        selectMusician("Willie Colón")
        onView(withId(R.id.add_members_button)).perform(click())

        awaitView(inMembers("Rubén Blades Bellido de Luna"))
        onView(inMembers("Willie Colón")).check(matches(isDisplayed()))
        onView(withText("Se agregaron 2 músicos a la banda")).check(matches(isDisplayed()))
        assertEquals(setOf("/bands/101/musicians/100", "/bands/101/musicians/102"), postPaths.toSet())
    }

    // PE-HU12-06
    @Test
    fun muestraErrorDelServidorYPermiteReintentar() {
        postStatus = 500
        enterAsCollector()
        openQueen()
        openAddMember()

        selectMusician("Willie Colón")
        onView(withId(R.id.add_members_button)).perform(click())
        onView(withId(R.id.submit_error)).check(matches(withText(R.string.error_server)))

        postStatus = 200
        onView(withId(R.id.add_members_button)).perform(click())

        awaitView(inMembers("Willie Colón"))
        assertEquals(2, postPaths.size)
    }

    // PE-HU12-07
    @Test
    fun cancelarRegresaAlDetalleSinAgregarMusicos() {
        enterAsCollector()
        openQueen()
        openAddMember()

        selectMusician("Willie Colón")
        onView(withId(R.id.cancel_button)).perform(click())

        onView(withId(R.id.musician_list)).check(doesNotExist())
        onView(inMembers("Willie Colón")).check(doesNotExist())
        assertTrue(postPaths.isEmpty())
    }

    // PE-HU12-08
    @Test
    fun avisaCuandoTodosLosMusicosYaPertenecenALaBanda() {
        memberIds.clear()
        memberIds += MUSICIANS.keys
        enterAsCollector()
        openQueen()
        onView(withId(R.id.add_member_button)).perform(scrollTo(), click())

        awaitView(withText(R.string.band_member_empty))
        onView(withId(R.id.add_members_button)).check(matches(not(isEnabled())))
    }

    private companion object {
        const val FREDDIE_ID = 104
        val MEMBER_POST = Regex("""/bands/101/musicians/(\d+)""")

        const val COLLECTORS =
            """[{"id":100,"name":"Manolo Bellon","email":"manollo@caracol.com.co","telephone":"3502457896"}]"""

        val MUSICIANS = linkedMapOf(
            100 to """{"id":100,"name":"Rubén Blades Bellido de Luna","image":"","description":"Cantante panameño.","birthDate":"1948-07-16T00:00:00.000Z"}""",
            102 to """{"id":102,"name":"Willie Colón","image":"","description":"Trombonista de salsa.","birthDate":"1950-04-28T00:00:00.000Z"}""",
            104 to """{"id":104,"name":"Freddie Mercury","image":"","description":"Vocalista de Queen.","birthDate":"1946-09-05T00:00:00.000Z"}""",
        )
    }
}
