package sudoku.domain.grading

import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuRules
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.GeneratorVersion

sealed interface GradingResult {
    data class Graded(
        val difficulty: Difficulty,
        val solution: SudokuBoard,
        val steps: List<LogicalStep>,
        val version: GeneratorVersion,
    ) : GradingResult {
        init {
            require(SudokuRules.isComplete(solution))
            require(steps.isNotEmpty())
        }
    }

    data class Unrated(val state: LogicalState, val steps: List<LogicalStep>) : GradingResult
    data object NotImplemented : GradingResult
}
