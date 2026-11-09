package sudoku.grading

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.game.SamplePuzzle
import sudoku.game.SudokuBoard

class LogicalStepTest {
    @Test(expected = IllegalArgumentException::class)
    fun rejectsStepsWithoutAnyChanges() {
        LogicalStep(SolvingTechnique.NAKED_PAIR)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsEliminationsWithoutAnyDigits() {
        LogicalStep(SolvingTechnique.NAKED_PAIR, eliminations = mapOf(0 to emptySet()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOutOfRangeCells() {
        LogicalStep(SolvingTechnique.NAKED_SINGLE, placements = mapOf(81 to 5))
    }

    @Test
    fun anAlreadyRemovedCandidateCannotBeCountedAgain() {
        val before = LogicalState.from(SudokuBoard.empty())
        val step = LogicalStep(SolvingTechnique.NAKED_PAIR, eliminations = mapOf(0 to setOf(5)))
        val remaining = before.candidates.toMutableList().apply { this[0] = this[0] - 5 }
        val after = before.copy(candidates = remaining)
        assertTrue(step.isApplicableTo(before))
        assertFalse(step.isApplicableTo(after))
    }

    @Test
    fun aPlacementMustActuallyFillAnEmptyCell() {
        val step = LogicalStep(SolvingTechnique.NAKED_SINGLE, placements = mapOf(0 to 5))
        assertTrue(step.isApplicableTo(LogicalState.from(SudokuBoard.empty())))
        assertFalse(step.isApplicableTo(LogicalState.from(SamplePuzzle.create().board)))
    }
}
