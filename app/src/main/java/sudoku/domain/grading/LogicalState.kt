package sudoku.domain.grading

import sudoku.domain.board.SudokuBoard
import sudoku.domain.game.GameNotes

data class LogicalState(val board: SudokuBoard, val candidates: List<Set<Int>>) {
    init {
        require(candidates.size == SudokuBoard.CELL_COUNT)
        require(candidates.all { values -> values.all { it in 1..SudokuBoard.SIZE } })
        require(candidates.indices.all { board[it] == 0 || candidates[it].isEmpty() })
    }

    companion object {
        // Initialize once; recomputing candidates after a step would restore logical eliminations.
        fun from(board: SudokuBoard): LogicalState = LogicalState(board, GameNotes.prefill(board))
    }
}
