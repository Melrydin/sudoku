package sudoku.domain.solver

import sudoku.domain.board.SudokuBoard
import sudoku.domain.common.OptionalValue

class SolutionResolver {
    fun resolve(puzzle: SudokuBoard): OptionalValue<SudokuBoard> = SolutionSearch(puzzle).solution()
}
