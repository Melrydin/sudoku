package sudoku.domain.game

import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuRules

object GameNotes {
    fun prefill(board: SudokuBoard): List<Set<Int>> = board.cells.indices.map { candidates(board, it) }

    fun candidates(board: SudokuBoard, index: Int): Set<Int> {
        if (board[index] != 0) return emptySet()
        val used = board.cells.indices.filter { SudokuRules.arePeers(index, it) }.map { board[it] }.toSet()
        return (1..SudokuBoard.SIZE).filterTo(mutableSetOf()) { it !in used }
    }

    fun afterEntry(state: GameState, value: Int, removeFromPeers: Boolean): List<Set<Int>> =
        state.notes.mapIndexed { index, notes ->
            when {
                index == state.selectedCell -> emptySet()
                removeFromPeers && SudokuRules.arePeers(index, state.selectedCell) -> notes - value
                else -> notes
            }
        }
}
