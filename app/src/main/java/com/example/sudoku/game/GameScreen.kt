package com.example.sudoku.game

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.sudoku.R
import com.example.sudoku.home.Difficulty
import com.google.android.material.button.MaterialButton

class GameScreen(
    private val root: View,
    private val session: GameSession,
    private val onChanged: () -> Unit,
) {
    private val board = root.findViewById<SudokuBoardView>(R.id.sudoku_board)
    private val notes = root.findViewById<MaterialButton>(R.id.notes)
    private val undo = root.findViewById<MaterialButton>(R.id.undo)

    fun bind(difficulty: Difficulty, onHome: () -> Unit, onTheme: () -> Unit, onNewGame: () -> Unit) {
        root.findViewById<TextView>(R.id.game_difficulty).setText(difficulty.label)
        root.findViewById<View>(R.id.back_home).setOnClickListener { onHome() }
        root.findViewById<View>(R.id.game_theme).setOnClickListener { onTheme() }
        root.findViewById<View>(R.id.restart_game).setOnClickListener { onNewGame() }
        notes.setOnClickListener { edit { toggleNotes() } }
        undo.setOnClickListener { edit { undo() } }
        board.onCellSelected = { index -> edit { select(index) } }
        NumberPad(root.findViewById(R.id.number_pad)) { number -> edit { enter(number) } }
            .bind { edit { erase() } }
        render()
    }

    fun render() {
        board.render(session.state)
        notes.isChecked = session.state.noteMode
        notes.contentDescription = root.context.getString(
            if (session.state.noteMode) R.string.notes_enabled else R.string.notes_disabled,
        )
        undo.isEnabled = session.canUndo
        root.findViewById<View>(R.id.game_completed).isVisible = session.state.isComplete
    }

    private fun edit(action: GameSession.() -> Unit) {
        session.action()
        render()
        onChanged()
    }
}
