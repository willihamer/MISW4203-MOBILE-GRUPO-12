package co.edu.uniandes.vinilos

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
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
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.util.Collections

/**
 * HU10 - Agregar artistas favoritos (escenarios PE-HU10-01 a PE-HU10-06).
 *
 * Usa MockWebServer: el coleccionista Ana (id 7) empieza sin favoritos, salvo que la prueba
 * los defina en [favoriteIds]. Los POST agregan el artista a esa lista, como hace BackVynils.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class HU10AddFavoriteArtistTest {
    private val server = MockWebServer()

    private val favoriteIds: MutableSet<Int> = Collections.synchronizedSet(linkedSetOf())
    private val postPaths: MutableList<String> = Collections.synchronizedList(mutableListOf())
    @Volatile private var postStatus = 200

    private val lifecycle = object : ExternalResource() {
        override fun before() {
            server.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path.orEmpty()
                    return when {
                        request.method == "POST" && FAVORITE_POST.matches(path) -> addFavorite(path)
                        path == "/collectors" -> json("[${collector()}]")
                        path == "/collectors/7" -> json(collector())
                        path == "/collectors/7/albums" -> json("[]")
                        path == "/collectors/7/performers" -> json("[${favorites()}]")
                        path == "/musicians" -> json("[$WILLIE]")
                        path == "/bands" -> json("[$FANIA]")
                        path == "/musicians/102" -> json(WILLIE)
                        path == "/bands/103" -> json(FANIA)
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

    private fun addFavorite(path: String): MockResponse {
        postPaths += path
        if (postStatus != 200) return MockResponse().setResponseCode(postStatus)
        val id = path.substringAfterLast('/').toInt()
        favoriteIds += id
        return json(if (id == 102) WILLIE else FANIA)
    }

    private fun favorites() = favoriteIds.joinToString(",") { if (it == 102) WILLIE else FANIA }

    private fun collector() =
        """{"id":7,"name":"Ana","telephone":"123","email":"ana@example.com","favoritePerformers":[${favorites()}]}"""

    private fun json(body: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(body)

    private fun openArtist(name: String, collector: Boolean = true) {
        if (collector) {
            onView(withText(R.string.welcome_collector_title)).perform(scrollTo(), click())
            awaitView(withText("Ana"), rootMatcher = isDialog())
            onView(withText("Ana")).inRoot(isDialog()).perform(click())
            onView(withId(R.id.continue_button)).perform(click())
        } else {
            onView(withText(R.string.welcome_visitor_title)).perform(click())
        }
        onView(withId(R.id.tab_artists)).perform(click())
        awaitView(withText(name))
        onView(withText(name)).perform(click())
        awaitView(allOf(withId(R.id.name), withText(name)))
    }

    private fun favoriteButtonShows(description: Int) {
        awaitView(allOf(withId(R.id.favorite_button), withContentDescription(description)))
    }

    // PE-HU10-01
    @Test
    fun visitanteNoVeLaOpcion() {
        openArtist(WILLIE_NAME, collector = false)

        onView(withId(R.id.favorite_button)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // PE-HU10-02
    @Test
    fun agregaUnMusicoAFavoritos() {
        openArtist(WILLIE_NAME)
        favoriteButtonShows(R.string.favorite_add)

        onView(withId(R.id.favorite_button)).perform(click())

        awaitView(withText(R.string.favorite_added))
        favoriteButtonShows(R.string.favorite_in_favorites)
        assertEquals(listOf("/collectors/7/musicians/102"), postPaths.toList())
    }

    // PE-HU10-03
    @Test
    fun agregaUnaBandaAFavoritos() {
        openArtist(FANIA_NAME)
        favoriteButtonShows(R.string.favorite_add)

        onView(withId(R.id.favorite_button)).perform(click())

        favoriteButtonShows(R.string.favorite_in_favorites)
        assertEquals(listOf("/collectors/7/bands/103"), postPaths.toList())
    }

    // PE-HU10-04
    @Test
    fun indicaSiYaEsFavoritoYNoLoVuelveAAgregar() {
        favoriteIds += 102
        openArtist(WILLIE_NAME)
        favoriteButtonShows(R.string.favorite_in_favorites)

        onView(withId(R.id.favorite_button)).perform(click())

        awaitView(withText(R.string.favorite_already_added))
        assertEquals(emptyList<String>(), postPaths.toList())
    }

    // PE-HU10-05
    @Test
    fun elFavoritoApareceEnElDetalleDelColeccionista() {
        openArtist(WILLIE_NAME)
        favoriteButtonShows(R.string.favorite_add)
        onView(withId(R.id.favorite_button)).perform(click())
        favoriteButtonShows(R.string.favorite_in_favorites)

        onView(withId(R.id.back_button)).perform(click())
        onView(withId(R.id.tab_collectors)).perform(click())
        awaitView(allOf(withText("Ana"), isDescendantOfA(withId(R.id.collector_list))))
        onView(allOf(withText("Ana"), isDescendantOfA(withId(R.id.collector_list)))).perform(click())

        awaitView(withId(R.id.favorite_artist_list))
        onView(withId(R.id.favorite_artist_list)).check(matches(hasDescendant(withText(WILLIE_NAME))))
    }

    // PE-HU10-06
    @Test
    fun unErrorDelServidorPermiteReintentar() {
        postStatus = 500
        openArtist(WILLIE_NAME)
        favoriteButtonShows(R.string.favorite_add)

        onView(withId(R.id.favorite_button)).perform(click())

        awaitView(withText(R.string.favorite_error))
        favoriteButtonShows(R.string.favorite_add)

        postStatus = 200
        onView(withId(R.id.favorite_button)).perform(click())

        favoriteButtonShows(R.string.favorite_in_favorites)
        onView(withId(R.id.favorite_button)).check(matches(isDisplayed()))
        assertEquals(2, postPaths.size)
    }

    companion object {
        private const val WILLIE_NAME = "Willie Colón"
        private const val FANIA_NAME = "Fania All-Stars"
        private val FAVORITE_POST = Regex("""^/collectors/7/(musicians|bands)/\d+$""")
        private const val WILLIE =
            """{"id":102,"name":"Willie Colón","image":"","description":"Trombonista y cantante de salsa.","birthDate":"1950-04-28T00:00:00.000Z","albums":[]}"""
        private const val FANIA =
            """{"id":103,"name":"Fania All-Stars","image":"","description":"Grupo de música latina.","creationDate":"1968-01-01T00:00:00.000Z","albums":[],"musicians":[]}"""
    }
}
