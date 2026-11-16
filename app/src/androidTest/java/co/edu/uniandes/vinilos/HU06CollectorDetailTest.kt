package co.edu.uniandes.vinilos

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import co.edu.uniandes.vinilos.testutil.VinilosTestRule
import co.edu.uniandes.vinilos.testutil.awaitView
import co.edu.uniandes.vinilos.testutil.scrollToBottom
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement

/** La prueba usa MockWebServer en lugar de BackVynils para controlar la respuesta HTTP. */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
private annotation class ConServidorSimulado

/**
 * HU06 - Consultar detalle de un coleccionista (escenarios PE-HU06-01 a PE-HU06-07).
 *
 * Las pruebas sin [ConServidorSimulado] requieren BackVynils con sus datos semilla en la URL
 * configurada por VINILOS_BASE_URL. En esos datos Manolo Bellon tiene el teléfono 3502457896,
 * el correo manollo@caracol.com.co, a Rubén Blades como favorito y el álbum "Buscando América".
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class HU06CollectorDetailTest {
    private val server = MockWebServer()
    @Volatile private var mocked = false
    @Volatile private var detailStatus = 200

    /** Arranca el servidor simulado solo en las pruebas que lo piden. */
    private val backend = TestRule { base, description ->
        object : Statement() {
            override fun evaluate() {
                mocked = description.getAnnotation(ConServidorSimulado::class.java) != null
                if (!mocked) return base.evaluate()
                server.dispatcher = object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse = respond(request.path.orEmpty())
                }
                server.start()
                try {
                    base.evaluate()
                } finally {
                    server.shutdown()
                }
            }
        }
    }
    private val app = VinilosTestRule { if (mocked) "http://localhost:${server.port}/" else BuildConfig.BASE_URL }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(backend).around(app)

    private fun respond(path: String): MockResponse = when (path) {
        "/collectors" -> json(COLLECTORS)
        "/collectors/7" -> json(SIN_COLECCION)
        "/collectors/42" -> json(ANA).setResponseCode(detailStatus)
        "/collectors/7/albums", "/collectors/42/albums" -> json("[]")
        else -> json("""{"statusCode":404,"message":"The collector with the given id was not found"}""").setResponseCode(404)
    }

    private fun json(value: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(value)

    private fun openCollector(name: String) {
        onView(withText(R.string.welcome_visitor_title)).perform(click())
        onView(withId(R.id.tab_collectors)).perform(click())
        awaitView(withText(name))
        onView(withText(name)).perform(click())
    }

    @Test
    fun muestraNombreTelefonoYCorreo() {
        openCollector(MANOLO)

        onView(withId(R.id.collector_name)).check(matches(allOf(withText(MANOLO), isDisplayed())))
        onView(withId(R.id.collector_phone)).check(matches(allOf(withText("3502457896"), isDisplayed())))
        onView(withId(R.id.collector_email)).check(matches(allOf(withText("manollo@caracol.com.co"), isDisplayed())))
    }

    @Test
    fun listaLosArtistasFavoritos() {
        openCollector(MANOLO)

        onView(withId(R.id.favorite_artist_list)).check(matches(hasDescendant(withText("Rubén Blades Bellido de Luna"))))
        onView(withId(R.id.favorites_empty)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun listaLosAlbumesDeLaColeccion() {
        openCollector(MANOLO)

        onView(withId(R.id.collection_album_list)).check(matches(hasDescendant(withText("Buscando América"))))
        onView(withId(R.id.collection_empty)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    @ConServidorSimulado
    fun muestraMensajesCuandoNoHayFavoritosNiAlbumes() {
        openCollector("José Pérez")

        onView(withId(R.id.collector_name)).check(matches(withText("José Pérez")))
        onView(withId(R.id.favorites_empty)).check(
            matches(allOf(withText(R.string.collector_favorites_empty), withEffectiveVisibility(Visibility.VISIBLE))),
        )
        onView(withId(R.id.collection_empty)).check(
            matches(allOf(withText(R.string.collector_albums_empty), withEffectiveVisibility(Visibility.VISIBLE))),
        )
        onView(withId(R.id.favorite_artist_list)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.collection_album_list)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    @ConServidorSimulado
    fun permiteReintentarDespuesDeUnError() {
        detailStatus = 500
        openCollector("Ana López")

        onView(withText(R.string.error_collector_title)).check(matches(isDisplayed()))
        onView(withText(R.string.error_server)).check(matches(isDisplayed()))

        detailStatus = 200
        onView(withId(R.id.error)).perform(scrollToBottom())
        onView(withId(R.id.retry_button)).perform(click())

        onView(withId(R.id.collector_name)).check(matches(allOf(withText("Ana López"), isDisplayed())))
        onView(withId(R.id.collector_phone)).check(matches(withText("456")))
    }

    @Test
    @ConServidorSimulado
    fun coleccionistaInexistenteMuestraMensaje() {
        openCollector("Coleccionista eliminado")

        onView(withText(R.string.error_collector_not_found)).check(matches(isDisplayed()))
        onView(withId(R.id.content)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun volverRegresaAlListado() {
        openCollector(MANOLO)
        onView(withId(R.id.collector_name)).check(matches(withText(MANOLO)))

        onView(withId(R.id.back_button)).perform(click())

        onView(withId(R.id.collector_list)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_bar)).check(matches(isDisplayed()))
    }

    private companion object {
        const val MANOLO = "Manolo Bellon"

        const val COLLECTORS = """[
            {"id":7,"name":"José Pérez","telephone":"123","email":"jose@example.com"},
            {"id":42,"name":"Ana López","telephone":"456","email":"ana@example.com"},
            {"id":99,"name":"Coleccionista eliminado","telephone":"789","email":"borrado@example.com"}
        ]"""
        const val SIN_COLECCION =
            """{"id":7,"name":"José Pérez","telephone":"123","email":"jose@example.com","favoritePerformers":[],"collectorAlbums":[]}"""
        const val ANA =
            """{"id":42,"name":"Ana López","telephone":"456","email":"ana@example.com","favoritePerformers":[],"collectorAlbums":[]}"""
    }
}
