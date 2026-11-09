package sudoku.generator

import sudoku.game.SudokuBoard

class SolutionCounter {
    fun count(board: SudokuBoard): SolutionCount = when (SolutionSearch(board).count()) {
        0 -> SolutionCount.NONE
        1 -> SolutionCount.UNIQUE
        else -> SolutionCount.MULTIPLE
    }
}
