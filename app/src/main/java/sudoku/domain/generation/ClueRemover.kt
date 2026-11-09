package sudoku.domain.generation

import kotlin.random.Random
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuRules
import sudoku.domain.common.checkCancellation
import sudoku.domain.solver.SolutionCount
import sudoku.domain.solver.SolutionCounter

class ClueRemover(
    private val countSolutions: (SudokuBoard) -> SolutionCount = SolutionCounter()::count,
    private val random: Random = Random(GeneratorConfig.APP_SEED),
) {
    fun removeClues(solution: SudokuBoard, targetClues: Int): ClueRemovalResult {
        require(SudokuRules.isComplete(solution)) { "Clue removal requires a complete, valid solution." }
        require(targetClues in 0..SudokuBoard.CELL_COUNT) { "The clue target must be between 0 and 81." }
        var puzzle = solution
        for (cell in solution.cells.indices.shuffled(random)) {
            checkCancellation()
            if (clueCount(puzzle) == targetClues) break
            val candidate = puzzle.withValue(cell, 0)
            when (countSolutions(candidate)) {
                SolutionCount.UNIQUE -> puzzle = candidate
                SolutionCount.MULTIPLE -> Unit
                SolutionCount.NONE -> error("Removing a clue cannot eliminate the original solution.")
                SolutionCount.NOT_IMPLEMENTED -> return ClueRemovalResult.CounterRequired
            }
        }
        return ClueRemovalResult.Removed(puzzle, clueCount(puzzle) == targetClues)
    }

    private fun clueCount(board: SudokuBoard): Int = board.cells.count { it != 0 }
}
