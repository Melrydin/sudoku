package com.example.sudoku.game

class GameSession(initialState: GameState, history: List<GameState> = emptyList(), initialElapsedMillis: Long = 0) {
    var state: GameState = initialState
        private set
    private val history = ArrayDeque(history)
    val canUndo: Boolean get() = history.isNotEmpty()
    val undoHistory: List<GameState> get() = history.toList()
    var elapsedMillis: Long = initialElapsedMillis
        private set

    init {
        require(initialElapsedMillis >= 0)
    }

    fun trackElapsedTime(millis: Long) {
        require(millis >= 0)
        elapsedMillis = millis
    }

    fun select(index: Int) {
        require(index in 0 until SudokuBoard.CELL_COUNT)
        state = state.copy(selectedCell = index)
    }

    fun toggleNotes() {
        state = state.copy(noteMode = !state.noteMode)
    }

    fun enter(value: Int, autoRemoveNotes: Boolean = false) {
        require(value in 1..SudokuBoard.SIZE)
        if (!canEditSelection()) return
        if (state.noteMode) enterNote(value) else commit(state.copy(
            board = state.board.withValue(state.selectedCell, value),
            notes = GameNotes.afterEntry(state, value, autoRemoveNotes),
        ))
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
        state.selectedCell >= 0 && state.puzzle[state.selectedCell] == 0 && !state.isComplete

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
    }
}
