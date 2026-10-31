package co.edu.uniandes.vinilos.testutil

import android.os.SystemClock
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Matcher

/** Espera una condición de UI, no un tiempo fijo: AsyncListDiffer termina fuera del hilo principal. */
fun awaitView(matcher: Matcher<View>, present: Boolean = true) {
    onView(isRoot()).perform(object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()
        override fun getDescription() = "Esperar hasta 5 segundos: $matcher, presente=$present"
        override fun perform(uiController: UiController, root: View) {
            val deadline = SystemClock.uptimeMillis() + 5_000
            do {
                uiController.loopMainThreadUntilIdle()
                val found = TreeIterables.breadthFirstViewTraversal(root).any { matcher.matches(it) }
                if (found == present) return
                uiController.loopMainThreadForAtLeast(16)
            } while (SystemClock.uptimeMillis() < deadline)
            throw AssertionError(description)
        }
    })
}
