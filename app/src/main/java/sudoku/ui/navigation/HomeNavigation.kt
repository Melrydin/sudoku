package sudoku.ui.navigation

import android.app.Activity
import android.content.Intent
import sudoku.app.appContainer
import sudoku.domain.puzzle.PuzzleId
import sudoku.ui.home.MainActivity

object HomeNavigation {
    fun open(activity: Activity, puzzle: PuzzleId) {
        activity.appContainer.appPreferences.apply {
            difficulty = puzzle.difficulty
            level = puzzle.level
        }
        activity.startActivity(Intent(activity, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
        activity.finish()
    }
}
