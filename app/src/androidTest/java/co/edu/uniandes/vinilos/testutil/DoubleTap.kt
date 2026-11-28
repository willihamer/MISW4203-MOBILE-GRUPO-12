package co.edu.uniandes.vinilos.testutil

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.Matcher

/**
 * Dos toques seguidos en el mismo ciclo del hilo principal, como el doble toque rápido de Monkey
 * (#136). El primero navega; el segundo llega cuando la app ya cambió de pantalla. Es determinista,
 * a diferencia de doubleClick(), que deja pasar un instante entre los toques.
 */
fun rapidDoubleTap(): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> = isDisplayed()

    override fun getDescription() = "dos toques seguidos sin esperar entre ellos"

    override fun perform(uiController: UiController, view: View) {
        view.performClick()
        view.performClick()
        uiController.loopMainThreadUntilIdle()
    }
}
