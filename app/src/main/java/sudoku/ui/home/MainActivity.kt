package sudoku.ui.home

import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import sudoku.R
import sudoku.app.appContainer
import sudoku.domain.game.findSavedPuzzle
import sudoku.domain.puzzle.PuzzleId
import sudoku.ui.common.ThemedActivity
import sudoku.ui.game.GameActivity

class MainActivity : ThemedActivity() {
    private val store by lazy { appContainer.games }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applySystemInsets(findViewById(R.id.main))
    }

    override fun onResume() {
        super.onResume()
        if (refreshTheme()) return
        HomeScreen(
            findViewById(R.id.main), preferences, ::selectTheme, ::startNewGame,
            ::continueGame, ::hasProgress,
        ).bind()
    }

    private fun startNewGame(puzzle: PuzzleId) {
        val state = store.findSavedPuzzle(puzzle)?.let { store.load(it)?.state }
        if (state == null || !state.hasEntries || state.isComplete) openNewGame(puzzle)
        else MaterialAlertDialogBuilder(this)
            .setTitle(R.string.restart_title)
            .setMessage(R.string.restart_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.new_game) { _, _ -> openNewGame(puzzle) }
            .show()
    }

    private fun openNewGame(puzzle: PuzzleId) {
        startActivity(GameActivity.intent(this, puzzle, true))
    }

    private fun continueGame(puzzle: PuzzleId) {
        val saved = store.findSavedPuzzle(puzzle) ?: return
        startActivity(GameActivity.intent(this, saved, false))
    }

    private fun hasProgress(puzzle: PuzzleId): Boolean =
        store.findSavedPuzzle(puzzle)?.let { store.load(it)?.state?.isComplete == false } == true
}
