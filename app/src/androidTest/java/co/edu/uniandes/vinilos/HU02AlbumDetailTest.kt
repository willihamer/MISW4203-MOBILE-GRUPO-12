package co.edu.uniandes.vinilos

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.testutil.VinilosTestRule
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** HU02 y recorridos HU01 → HU02 → HU04 y HU03 → HU04 → HU02, contra BackVynils. */
@RunWith(AndroidJUnit4::class)
class HU02AlbumDetailTest {
    @get:Rule val vinilos = VinilosTestRule()
    @Before fun enter() {
        onView(withText(R.string.welcome_visitor_title)).perform(click())
    }
    private fun openAlbum() { onView(withText("Buscando América")).perform(click()) }

    @Test fun muestraInformacionCompletaYTracks() {
        openAlbum()
        onView(withId(R.id.album_name)).check(matches(withText("Buscando América")))
        onView(withId(R.id.cover)).check(matches(isDisplayed()))
        onView(withText("Rubén Blades Bellido de Luna")).check(matches(isDisplayed()))
        onView(withId(R.id.description)).check(matches(withText(org.hamcrest.Matchers.startsWith("Buscando América es el primer álbum"))))
        onView(withId(R.id.release_date)).check(matches(withText("Lanzamiento: 01/08/1984")))
        onView(withId(R.id.genre)).check(matches(withText("Género: Salsa")))
        onView(withId(R.id.record_label)).check(matches(withText("Sello: Elektra")))
        onView(withText("Decisiones · 5:05")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText("Desapariciones · 6:29")).perform(scrollTo()).check(matches(isDisplayed()))
    }
    @Test fun albumSinTracksMuestraMensaje() {
        onView(withText("Poeta del pueblo")).perform(click())
        onView(withId(R.id.tracks_empty)).perform(scrollTo()).check(matches(isDisplayed()))
    }
    @Test fun recrearActivityConservaElDetalle() {
        openAlbum()
        vinilos.scenario.recreate()
        onView(withId(R.id.album_name)).check(matches(withText("Buscando América")))
        pressBack()
        onView(withId(R.id.album_list)).check(matches(isDisplayed()))
    }
    @Test fun cerrarRegresaAlCatalogo() {
        openAlbum()
        onView(withId(R.id.close_button)).perform(click())
        onView(withId(R.id.album_list)).check(matches(isDisplayed()))
    }
    @Test fun abrirArtistaYVolverConservaElAlbum() {
        openAlbum()
        onView(withText("Rubén Blades Bellido de Luna")).perform(scrollTo(), click())
        onView(withId(R.id.date_chip)).check(matches(withText("Nacimiento: 16/07/1948")))
        pressBack()
        onView(withId(R.id.album_name)).check(matches(withText("Buscando América")))
    }
    @Test fun abrirAlbumDesdeArtistaYVolver() {
        onView(withId(R.id.tab_artists)).perform(click())
        onView(withText("Rubén Blades Bellido de Luna")).perform(click())
        onView(withText("Buscando América")).perform(scrollTo(), click())
        onView(withId(R.id.album_name)).check(matches(withText("Buscando América")))
        pressBack()
        onView(allOf(withId(R.id.name), withText("Rubén Blades Bellido de Luna"))).check(matches(isDisplayed()))
    }
}
