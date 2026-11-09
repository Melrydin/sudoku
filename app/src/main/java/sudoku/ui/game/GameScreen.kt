package sudoku.ui.game

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import sudoku.R
import sudoku.domain.game.GameSession
import sudoku.domain.puzzle.PuzzleId
import sudoku.domain.settings.GameOptions
import sudoku.ui.common.label
import sudoku.ui.game.board.SudokuBoardView
import sudoku.ui.game.controls.NumberPad

class GameScreen(
    private val root: View,
    private val session: GameSession,
    private val onChanged: () -> Unit,
    private val options: () -> GameOptions = { GameOptions() },
) {
    private val board = root.findViewById<SudokuBoardView>(R.id.sudoku_board)
    private val notes = root.findViewById<MaterialButton>(R.id.notes)
    private val undo = root.findViewById<MaterialButton>(R.id.undo)
    private val numberPad = NumberPad(root.findViewById(R.id.number_pad)) { number ->
        edit { enter(number, options().autoRemoveNotes) }
    }

    fun bind(
        puzzle: PuzzleId, onHome: () -> Unit, onTheme: () -> Unit,
        onNewGame: () -> Unit, onSettings: () -> Unit,
    ) {
        renderTitle(puzzle)
        root.findViewById<View>(R.id.back_home).setOnClickListener { onHome() }
        root.findViewById<View>(R.id.game_theme).setOnClickListener { onTheme() }
        root.findViewById<View>(R.id.restart_game).setOnClickListener { onNewGame() }
        root.findViewById<View>(R.id.game_settings).setOnClickListener { onSettings() }
        notes.setOnClickListener { edit { toggleNotes() } }
        undo.setOnClickListener { edit { undo() } }
        board.onCellSelected = { index -> edit { select(index) } }
        board.onCellErased = { index -> edit { select(index); erase() } }
        numberPad.bind { edit { erase() } }
        render()
    }

    fun render() {
        val settings = options()
        board.render(session.state, settings)
        numberPad.render(session.state.board, settings)
        root.findViewById<View>(R.id.game_timer).isVisible = settings.showTimer
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

    private fun renderTitle(puzzle: PuzzleId) {
        root.findViewById<TextView>(R.id.sample_puzzle_label).setText(R.string.unrated_puzzle)
        root.findViewById<TextView>(R.id.game_difficulty).text = root.context.getString(
            R.string.game_identity, root.context.getString(puzzle.difficulty.label), puzzle.level,
        )
    }
}
