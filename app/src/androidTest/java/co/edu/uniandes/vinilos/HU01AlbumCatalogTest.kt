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
import co.edu.uniandes.vinilos.testutil.awaitView
import co.edu.uniandes.vinilos.testutil.VinilosTestRule
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * HU01 - Consultar catálogo de álbumes.
 *
 * Requiere BackVynils con sus datos semilla en la URL configurada por VINILOS_BASE_URL.
 */
@RunWith(AndroidJUnit4::class)
class HU01AlbumCatalogTest {

    @get:Rule
    val vinilos = VinilosTestRule()

    @Before
    fun enterAsVisitor() {
        onView(withText(R.string.welcome_visitor_title)).perform(click())
    }

    @Test
    fun muestraElCatalogoConNombreArtistaAnioYGenero() {
        onView(withId(R.id.album_list)).check(matches(isDisplayed()))
        onView(withText("Buscando América")).check(matches(isDisplayed()))
        onView(withId(R.id.album_list)).check(
            matches(hasDescendant(allOf(withId(R.id.artist), withText("Rubén Blades Bellido de Luna")))),
        )
        onView(withId(R.id.album_list)).check(
            matches(hasDescendant(allOf(withId(R.id.meta), withText("1975 • Rock")))),
        )
        onView(withId(R.id.album_list)).check(
            matches(hasDescendant(allOf(withId(R.id.meta), withText("1984 • Salsa")))),
        )
    }

    // PE-HU01-01: recorrer los cuatro álbumes, incluso fuera del área visible.
    @Test
    fun recorreLosCuatroAlbumesDelCatalogo() {
        listOf("Buscando América", "Poeta del pueblo", "A Night at the Opera", "A Day at the Races").forEach { name ->
            onView(withId(R.id.album_list)).perform(
                androidx.test.espresso.contrib.RecyclerViewActions.scrollTo<androidx.recyclerview.widget.RecyclerView.ViewHolder>(
                    hasDescendant(withText(name)),
                ),
            )
            onView(withText(name)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun filtraPorGenero() {
        onView(withText(R.string.genre_rock)).perform(click())

        onView(withText("A Night at the Opera")).check(matches(isDisplayed()))
        awaitView(withText("Buscando América"), present = false)
        onView(withText("Buscando América")).check(doesNotExist())
    }

    @Test
    fun buscaPorArtistaSinTildes() {
        onView(withId(R.id.search_field)).perform(replaceText("ruben"), closeSoftKeyboard())

        onView(withText("Buscando América")).check(matches(isDisplayed()))
        awaitView(withText("A Night at the Opera"), present = false)
        onView(withText("A Night at the Opera")).check(doesNotExist())
    }

    @Test
    fun informaCuandoNingunAlbumCoincide() {
        onView(withId(R.id.search_field)).perform(replaceText("zzzz"), closeSoftKeyboard())

        onView(withText(R.string.albums_no_results_title)).check(matches(isDisplayed()))
    }

    @Test
    fun muestraLaBarraDeNavegacionConLasTresPestanas() {
        onView(withId(R.id.bottom_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.tab_artists)).perform(click())
        onView(withId(R.id.artist_list)).check(matches(isDisplayed()))

        onView(withId(R.id.tab_albums)).perform(click())
        onView(withText("Buscando América")).check(matches(isDisplayed()))
    }
}
