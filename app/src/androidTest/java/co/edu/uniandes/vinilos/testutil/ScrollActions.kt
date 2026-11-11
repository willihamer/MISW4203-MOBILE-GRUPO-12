package co.edu.uniandes.vinilos.testutil

import android.view.View
import android.widget.ScrollView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import org.hamcrest.Matcher

/**
 * Lleva un ScrollView hasta el final. scrollTo() deja la vista justo en el borde inferior, donde
 * en pantallas bajas queda bajo la barra flotante; al final del contenido sí se respeta el espacio
 * reservado para ella (vn_bottom_bar_clearance).
 */
fun scrollToBottom(): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> = isAssignableFrom(ScrollView::class.java)
    override fun getDescription() = "desplazar el ScrollView hasta el final"
    override fun perform(uiController: UiController, view: View) {
        // Inmediato: fullScroll() anima el desplazamiento y la prueba revisaría antes de que termine
        val scrollView = view as ScrollView
        scrollView.scrollTo(0, scrollView.getChildAt(0).height)
        uiController.loopMainThreadUntilIdle()
    }
}
