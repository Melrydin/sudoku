package sudoku.grading

import sudoku.game.Difficulty
import sudoku.game.SudokuBoard
import sudoku.game.SudokuRules
import sudoku.generator.GeneratorVersion

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
