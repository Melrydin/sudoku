package sudoku.domain.grading

import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.GeneratorVersion
import sudoku.fixtures.TestBoards

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
