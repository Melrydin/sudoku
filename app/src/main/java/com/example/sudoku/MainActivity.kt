package com.example.sudoku

import android.os.Bundle
import com.example.sudoku.game.GameActivity
import com.example.sudoku.game.GameStore
import com.example.sudoku.home.Difficulty
import com.example.sudoku.home.HomeScreen
import com.example.sudoku.ui.ThemedActivity
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
            { difficulty -> startActivity(GameActivity.intent(this, difficulty, false)) },
            { difficulty -> store.load(difficulty)?.state?.isComplete == false },
        ).bind()
    }

    private fun startNewGame(difficulty: Difficulty) {
        val state = store.load(difficulty)?.state
        if (state == null || !state.hasEntries || state.isComplete) openNewGame(difficulty)
        else MaterialAlertDialogBuilder(this)
            .setTitle(R.string.restart_title)
            .setMessage(R.string.restart_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.new_game) { _, _ -> openNewGame(difficulty) }
            .show()
    }

    private fun openNewGame(difficulty: Difficulty) {
        startActivity(GameActivity.intent(this, difficulty, true))
    }
}
