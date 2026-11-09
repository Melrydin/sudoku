package com.example.sudoku.settings

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.sudoku.R
import com.example.sudoku.game.BoardLayoutController
import com.example.sudoku.game.BoardLayoutOptions
import com.example.sudoku.game.GameScreen
import com.example.sudoku.game.GameSession
import com.example.sudoku.game.GameStore
import com.example.sudoku.game.PlayTimeFormatter
import com.example.sudoku.game.SamplePuzzle
import com.example.sudoku.ui.ThemedActivity

class LayoutPreviewActivity : ThemedActivity() {
    private val gamePreferences by lazy { GamePreferences(this) }
    private var draft = BoardLayoutOptions()
    private lateinit var dialog: BoardLayoutDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val root = findViewById<View>(R.id.game_root)
        applySystemInsets(root)
        val session = GameStore(this).load(preferences.difficulty) ?: GameSession(SamplePuzzle.create())
        GameScreen(root, session, {}, { gamePreferences.options }).bind(preferences.difficulty, {}, {}, {}, {})
        findViewById<TextView>(R.id.game_timer).text = PlayTimeFormatter.format(session.elapsedMillis)
        val layout = BoardLayoutController(root)
        draft = savedInstanceState?.let(::restoreDraft) ?: gamePreferences.boardLayout
        dialog = BoardLayoutDialog(this, gamePreferences, { draft = it; layout.apply(it) }, ::finish)
        dialog.show(draft)
    }

    override fun onDestroy() {
        if (::dialog.isInitialized) dialog.release()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat("width", draft.widthFraction)
        outState.putFloat("vertical", draft.verticalBias)
        outState.putFloat("horizontal", draft.horizontalBias)
        super.onSaveInstanceState(outState)
    }

    private fun restoreDraft(state: Bundle): BoardLayoutOptions = BoardLayoutOptions(
        state.getFloat("width", 1f), state.getFloat("vertical", 0.5f), state.getFloat("horizontal", 0.5f),
    )
}
