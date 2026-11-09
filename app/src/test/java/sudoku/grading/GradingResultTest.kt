package sudoku.grading

import org.junit.Test
import sudoku.game.Difficulty
import sudoku.game.SudokuBoard
import sudoku.game.TestBoards
import sudoku.generator.GeneratorVersion

class GradingResultTest {
    @Test(expected = IllegalArgumentException::class)
    fun anIncompleteBoardCannotBeClassifiedAsGraded() {
        GradingResult.Graded(Difficulty.BEGINNER, SudokuBoard.empty(),
            listOf(LogicalStep(SolvingTechnique.NAKED_SINGLE, placements = mapOf(0 to 5))), GeneratorVersion.V1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun gradingRequiresARecordedStep() {
        GradingResult.Graded(Difficulty.BEGINNER, TestBoards.solved(), emptyList(), GeneratorVersion.V1)
    }
}
