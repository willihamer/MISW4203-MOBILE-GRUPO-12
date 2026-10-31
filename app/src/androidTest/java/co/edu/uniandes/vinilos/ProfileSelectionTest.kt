package co.edu.uniandes.vinilos

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.testutil.VinilosTestRule
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
        onView(withText(R.string.welcome_collector_title)).perform(click())

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

        onView(withText(R.string.welcome_collector_title)).check(matches(isDisplayed()))
    }
}
