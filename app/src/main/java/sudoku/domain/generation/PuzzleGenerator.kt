package sudoku.domain.generation

import sudoku.domain.board.SudokuBoard
import sudoku.domain.common.checkCancellation

class PuzzleGenerator(
    private val generateSolution: () -> SudokuBoard = { SudokuGenerator().generate() },
    private val remover: ClueRemover = ClueRemover(),
) {
    fun generate(targetClues: Int, maxAttempts: Int = 1): ClueRemovalResult {
        require(targetClues in 0 until SudokuBoard.CELL_COUNT) { "A playable puzzle needs an empty cell." }
        require(maxAttempts > 0) { "At least one generation attempt is required." }
        var result: ClueRemovalResult = ClueRemovalResult.CounterRequired
        repeat(maxAttempts) {
            checkCancellation()
            val attempt = remover.removeClues(generateSolution(), targetClues)
            if (attempt !is ClueRemovalResult.Removed || attempt.targetReached) return attempt
            result = attempt
        }
        return result
    }
}
