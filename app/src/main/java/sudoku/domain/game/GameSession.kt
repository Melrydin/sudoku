package sudoku.domain.game

import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuRules
import sudoku.domain.solver.SolutionResolver

class GameSession(
    initialState: GameState,
    history: List<GameState> = emptyList(),
    initialElapsedMillis: Long = 0,
    initialSolution: SudokuBoard? = null,
    initialMistakes: Int = 0,
) {
    val solution: SudokuBoard = initialSolution ?: requireNotNull(SolutionResolver().resolve(initialState.puzzle))
    var state: GameState = initialState
        private set
    private val history = ArrayDeque(history)
    var mistakes: Int = initialMistakes
        private set
    val remainingLives: Int get() = MAX_MISTAKES - mistakes
    val isGameOver: Boolean get() = mistakes == MAX_MISTAKES
    val canUndo: Boolean get() = history.isNotEmpty() && !isGameOver
    val undoHistory: List<GameState> get() = history.toList()
    val incorrectCells: Set<Int>
        get() = state.board.cells.indices.filterTo(mutableSetOf()) {
            state.board[it] != 0 && state.board[it] != solution[it]
        }
    var elapsedMillis: Long = initialElapsedMillis
        private set

    init {
        require(initialElapsedMillis >= 0)
        require(initialMistakes in 0..MAX_MISTAKES)
        require(SudokuRules.isComplete(solution))
        require(state.puzzle.cells.indices.all { state.puzzle[it] == 0 || state.puzzle[it] == solution[it] })
        if (isGameOver) resetAfterLoss()
    }

    fun trackElapsedTime(millis: Long) {
        require(millis >= 0)
        if (isGameOver) return
        elapsedMillis = millis
    }

    fun select(index: Int) {
        require(index in 0 until SudokuBoard.CELL_COUNT)
        if (isGameOver) return
        state = state.copy(selectedCell = index)
    }

    fun toggleNotes() {
        if (isGameOver) return
        state = state.copy(noteMode = !state.noteMode)
    }

    fun enter(value: Int, autoRemoveNotes: Boolean = false) {
        require(value in 1..SudokuBoard.SIZE)
        if (!canEditSelection()) return
        if (state.noteMode) enterNote(value) else enterDigit(value, autoRemoveNotes)
    }

    private fun enterDigit(value: Int, autoRemoveNotes: Boolean) {
        commit(state.copy(
            board = state.board.withValue(state.selectedCell, value),
            notes = GameNotes.afterEntry(state, value, autoRemoveNotes),
        ))
        if (value != solution[state.selectedCell]) {
            mistakes++
            if (isGameOver) resetAfterLoss()
        }
    }

    fun erase() {
        if (!canEditSelection()) return
        commit(state.copy(board = state.board.withValue(state.selectedCell, 0), notes = updatedNotes(emptySet())))
    }

    fun undo() {
        if (!canUndo) return
        state = history.removeLast().copy(selectedCell = state.selectedCell, noteMode = state.noteMode)
    }

    private fun canEditSelection(): Boolean =
        state.selectedCell >= 0 && state.puzzle[state.selectedCell] == 0 && !state.isComplete && !isGameOver

    private fun resetAfterLoss() {
        state = GameState(state.puzzle)
        history.clear()
        elapsedMillis = 0
    }

    private fun enterNote(value: Int) {
        if (state.board[state.selectedCell] != 0) return
        val current = state.notes[state.selectedCell]
        val next = if (value in current) current - value else current + value
        commit(state.copy(notes = updatedNotes(next)))
    }

    private fun updatedNotes(notes: Set<Int>): List<Set<Int>> =
        state.notes.toMutableList().apply { this[state.selectedCell] = notes.toSet() }

    private fun commit(next: GameState) {
        if (state.board.cells == next.board.cells && state.notes == next.notes) return
        history.addLast(state)
        if (history.size > MAX_HISTORY) history.removeFirst()
        state = next
    }

    companion object {
        const val MAX_HISTORY: Int = 100
        const val MAX_MISTAKES: Int = 3
    }
}
