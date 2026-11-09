package sudoku

import android.os.SystemClock
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import org.hamcrest.Matcher

internal fun waitForGame() {
    onView(isRoot()).perform(WaitForGame())
}

private class WaitForGame : ViewAction {
    override fun getDescription(): String = "Wait for the generated board to be displayed."

    override fun getConstraints(): Matcher<View> = isRoot()

    override fun perform(uiController: UiController, view: View) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (view.rootView.findViewById<View>(R.id.sudoku_board) == null) {
            check(SystemClock.uptimeMillis() < deadline) { "The generated board did not appear." }
            uiController.loopMainThreadForAtLeast(50)
        }
        uiController.loopMainThreadUntilIdle()
    }
}
