package sudoku

import android.os.Bundle
import sudoku.game.GameActivity
import sudoku.game.GameStore
import sudoku.game.PuzzleId
import sudoku.home.HomeScreen
import sudoku.ui.ThemedActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : ThemedActivity() {
    private val store by lazy { GameStore(this) }

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
            { puzzle -> startActivity(GameActivity.intent(this, puzzle, false)) },
            { puzzle -> store.load(puzzle)?.state?.isComplete == false },
        ).bind()
    }

    private fun startNewGame(puzzle: PuzzleId) {
        val state = store.load(puzzle)?.state
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
}
