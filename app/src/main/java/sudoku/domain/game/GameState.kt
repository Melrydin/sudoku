package sudoku.domain.game

import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuRules

data class GameState(
    val puzzle: SudokuBoard,
    val board: SudokuBoard = puzzle,
    val notes: List<Set<Int>> = List(SudokuBoard.CELL_COUNT) { emptySet() },
    val selectedCell: Int = -1,
    val noteMode: Boolean = false,
) {
    init {
        require(selectedCell in -1 until SudokuBoard.CELL_COUNT)
        require(notes.size == SudokuBoard.CELL_COUNT && notes.all { cell -> cell.all { it in 1..9 } })
        require(puzzle.cells.indices.all { puzzle[it] == 0 || puzzle[it] == board[it] })
        require(notes.indices.all { board[it] == 0 || notes[it].isEmpty() })
        require(SudokuRules.conflicts(puzzle).isEmpty())
    }

    val hasEntries: Boolean
        get() = board.cells != puzzle.cells || notes.any { it.isNotEmpty() }

    val isComplete: Boolean
        get() = SudokuRules.isComplete(board)
}
