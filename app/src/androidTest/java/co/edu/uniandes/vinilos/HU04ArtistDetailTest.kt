package co.edu.uniandes.vinilos

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
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
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * HU04 - Consultar detalle de un artista.
 *
 * Requiere BackVynils con sus datos semilla en la URL configurada por VINILOS_BASE_URL. En esos datos Rubén Blades tiene
 * los álbumes "Buscando América" y "Poeta del pueblo", y Queen "A Night at the Opera" y
 * "A Day at the Races", sin integrantes registrados.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class HU04ArtistDetailTest {

    @get:Rule
    val vinilos = VinilosTestRule()

    @Before
    fun enterAsVisitorAndNavigateToArtists() {
        onView(withText(R.string.welcome_visitor_title)).perform(click())
        onView(withId(R.id.tab_artists)).perform(click())
    }

    @Test
    fun muestraElDetalleDelMusicoConSusAlbumes() {
        onView(withText(RUBEN_BLADES)).perform(click())

        onView(allOf(withId(R.id.name), withText(RUBEN_BLADES))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.type_badge), withText(R.string.artist_type_musician))).check(matches(isDisplayed()))
        onView(withId(R.id.date_chip)).check(matches(withText("Nacimiento: 16/07/1948")))
        onView(withId(R.id.album_list)).check(matches(hasDescendant(withText("Buscando América"))))
        onView(withId(R.id.album_list)).check(matches(hasDescendant(withText("Poeta del pueblo"))))
        // Los músicos no tienen sección de integrantes
        onView(withId(R.id.members_section)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun muestraElDetalleDeLaBandaConSusAlbumesEIntegrantes() {
        onView(withText(QUEEN)).perform(click())

        onView(allOf(withId(R.id.name), withText(QUEEN))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.type_badge), withText(R.string.artist_type_band))).check(matches(isDisplayed()))
        onView(withId(R.id.date_chip)).check(matches(withText("Creación: 01/01/1970")))
        onView(withId(R.id.album_list)).check(matches(hasDescendant(withText("A Night at the Opera"))))
        // Las bandas muestran la sección de integrantes; Queen no tiene integrantes en los datos de prueba
        onView(withId(R.id.members_section)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.members_empty)).check(matches(withText(R.string.artist_members_empty)))
    }

    @Test
    fun elBotonVolverRegresaAlListadoDeArtistas() {
        onView(withText(QUEEN)).perform(click())
        onView(allOf(withId(R.id.name), withText(QUEEN))).check(matches(isDisplayed()))

        onView(withId(R.id.back_button)).perform(click())

        onView(withId(R.id.artist_list)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_bar)).check(matches(isDisplayed()))
    }

    @Test
    fun laTeclaAtrasDelSistemaRegresaAlListadoDeArtistas() {
        onView(withText(RUBEN_BLADES)).perform(click())
        onView(allOf(withId(R.id.name), withText(RUBEN_BLADES))).check(matches(isDisplayed()))

        pressBack()

        onView(withId(R.id.artist_list)).check(matches(isDisplayed()))
    }

    private companion object {
        const val RUBEN_BLADES = "Rubén Blades Bellido de Luna"
        const val QUEEN = "Queen"
    }
}
