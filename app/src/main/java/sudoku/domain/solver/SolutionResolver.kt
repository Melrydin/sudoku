package sudoku.domain.solver

import sudoku.domain.board.SudokuBoard

class SolutionResolver {
    fun resolve(puzzle: SudokuBoard): SudokuBoard? = SolutionSearch(puzzle).solution()
}
