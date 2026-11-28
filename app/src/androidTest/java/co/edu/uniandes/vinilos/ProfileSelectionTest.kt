package co.edu.uniandes.vinilos

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.testutil.VinilosTestRule
import co.edu.uniandes.vinilos.testutil.rapidDoubleTap
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Selección de perfil (P01 y P01b): visitante o coleccionista.
 *
 * Requiere BackVynils corriendo con sus datos de prueba (coleccionista "Manolo Bellon").
 */
@RunWith(AndroidJUnit4::class)
class ProfileSelectionTest {

    @get:Rule
    val vinilos = VinilosTestRule()

    @Test
    fun elVisitanteEntraAlCatalogo() {
        onView(withText(R.string.welcome_visitor_title)).perform(click())

        onView(withId(R.id.album_list)).check(matches(isDisplayed()))
        onView(withId(R.id.profile_name)).check(matches(withText(R.string.profile_visitor)))
    }

    @Test
    fun elColeccionistaEscogeSuPerfilYContinua() {
        onView(withText(R.string.welcome_collector_title)).perform(scrollTo(), click())

        onView(withText(R.string.collector_picker_title)).check(matches(isDisplayed()))
        onView(withId(R.id.continue_button)).check(matches(not(isEnabled())))

        onView(withText("Manolo Bellon")).perform(click())
        onView(withId(R.id.continue_button)).check(matches(isEnabled())).perform(click())

        onView(withId(R.id.album_list)).check(matches(isDisplayed()))
        onView(withId(R.id.profile_name)).check(matches(withText("Manolo B.")))
        onView(withId(R.id.profile_role)).check(matches(withText(R.string.profile_collector)))
    }

    @Test
    fun elChipDePerfilPermiteCambiarDePerfil() {
        onView(withText(R.string.welcome_visitor_title)).perform(click())
        onView(withId(R.id.profile_chip)).perform(click())

        onView(withText(R.string.welcome_collector_title)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    // #136: Monkey cerraba la app con un doble toque rápido. El segundo toque pedía una navegación
    // que ya no existía desde la pantalla nueva (IllegalArgumentException de NavController).

    @Test
    fun dobleToqueEnColeccionistaAbreUnSoloSelector() {
        onView(withId(R.id.option_collector)).perform(scrollTo(), rapidDoubleTap())

        onView(withText(R.string.collector_picker_title)).check(matches(isDisplayed()))
    }

    @Test
    fun dobleToqueEnVisitanteEntraUnaSolaVezAlCatalogo() {
        onView(withId(R.id.option_visitor)).perform(rapidDoubleTap())

        onView(withId(R.id.album_list)).check(matches(isDisplayed()))
        onView(withId(R.id.profile_name)).check(matches(withText(R.string.profile_visitor)))
    }

    @Test
    fun dobleToqueEnUnAlbumAbreElDetalleUnaSolaVez() {
        onView(withId(R.id.option_visitor)).perform(click())
        onView(withId(R.id.album_list))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, rapidDoubleTap()))
        onView(withId(R.id.album_name)).check(matches(isDisplayed()))

        // Si el detalle se hubiera abierto dos veces, atrás mostraría otra vez el mismo detalle.
        pressBack()
        onView(withId(R.id.album_list)).check(matches(isDisplayed()))
    }
}
