package co.edu.uniandes.vinilos

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.testutil.VinilosTestRule
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * HU03 - Consultar listado de artistas.
 *
 * Requiere BackVynils corriendo en el computador (puerto 3000) con sus datos de prueba,
 * accesible desde el emulador en http://10.0.2.2:3000/.
 */
@RunWith(AndroidJUnit4::class)
class HU03ArtistListTest {

    @get:Rule
    val vinilos = VinilosTestRule()

    @Before
    fun enterAsVisitorAndNavigateToArtists() {
        onView(withText(R.string.welcome_visitor_title)).perform(click())
        onView(withText(R.string.nav_artists)).perform(click())
    }

    @Test
    fun muestraElListadoDeArtistasConMusicosYBandas() {
        onView(withId(R.id.artist_list)).check(matches(isDisplayed()))

        // Verifica presencia de un músico (Rubén Blades) y su etiqueta "Músico"
        onView(withText("Rubén Blades Bellido de Luna")).check(matches(isDisplayed()))
        onView(withId(R.id.artist_list)).check(
            matches(hasDescendant(allOf(withId(R.id.type_badge), withText(R.string.artist_type_musician)))),
        )

        // Verifica presencia de una banda (Queen) y su etiqueta "Banda"
        onView(withText("Queen")).check(matches(isDisplayed()))
        onView(withId(R.id.artist_list)).check(
            matches(hasDescendant(allOf(withId(R.id.type_badge), withText(R.string.artist_type_band)))),
        )
    }

    @Test
    fun filtraPorTipoMusicosYBandas() {
        // Filtrar por Músicos
        onView(withText(R.string.artists_filter_musicians)).perform(click())
        onView(withText("Rubén Blades Bellido de Luna")).check(matches(isDisplayed()))
        onView(withText("Queen")).check(doesNotExist())

        // Filtrar por Bandas
        onView(withText(R.string.artists_filter_bands)).perform(click())
        onView(withText("Queen")).check(matches(isDisplayed()))
        onView(withText("Rubén Blades Bellido de Luna")).check(doesNotExist())

        // Volver a Todos
        onView(withText(R.string.artists_filter_all)).perform(click())
        onView(withText("Rubén Blades Bellido de Luna")).check(matches(isDisplayed()))
        onView(withText("Queen")).check(matches(isDisplayed()))
    }

    @Test
    fun buscaPorNombreDeArtistaSinTildes() {
        onView(withId(R.id.search_field)).perform(replaceText("ruben"), closeSoftKeyboard())

        onView(withText("Rubén Blades Bellido de Luna")).check(matches(isDisplayed()))
        onView(withText("Queen")).check(doesNotExist())
    }

    @Test
    fun informaCuandoNingunArtistaCoincide() {
        onView(withId(R.id.search_field)).perform(replaceText("zzzz"), closeSoftKeyboard())

        onView(withText(R.string.artists_no_results_title)).check(matches(isDisplayed()))
    }

    @Test
    fun navegaEntreCatalogoDeAlbumesYArtistas() {
        onView(withId(R.id.bottom_bar)).check(matches(isDisplayed()))

        onView(withText(R.string.nav_albums)).perform(click())
        onView(withText("Buscando América")).check(matches(isDisplayed()))

        onView(withText(R.string.nav_artists)).perform(click())
        onView(withText("Rubén Blades Bellido de Luna")).check(matches(isDisplayed()))
    }
}
