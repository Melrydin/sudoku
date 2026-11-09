package sudoku.domain.solver

import sudoku.domain.board.SudokuBoard

class SolutionCounter {
    fun count(board: SudokuBoard): SolutionCount = when (SolutionSearch(board).count()) {
        0 -> SolutionCount.NONE
        1 -> SolutionCount.UNIQUE
        else -> SolutionCount.MULTIPLE
    }
}
