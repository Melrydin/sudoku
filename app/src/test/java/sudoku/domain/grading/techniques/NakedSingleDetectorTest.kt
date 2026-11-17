package sudoku.domain.grading.techniques

import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.fixtures.LogicalStates
import sudoku.fixtures.TestBoards
import sudoku.testing.TechniqueAssertions

class NakedSingleDetectorTest {
    private val checks: TechniqueAssertions = TechniqueAssertions(NakedSingleDetector(), SolvingTechnique.NAKED_SINGLE)
    private val empty: LogicalState = LogicalState.from(SudokuBoard.empty())

    @Test
    fun findsEveryDigitInEveryCellWithoutRecomputingCandidates() {
        for (cell: Int in 0 until SudokuBoard.CELL_COUNT) {
            for (digit: Int in 1..SudokuBoard.SIZE) {
                checks.assertPlacement(
                    LogicalStates.withCandidates(empty, cell to setOf(digit)),
                    cell,
                    digit
                )
            }
        }
    }

    @Test
    fun returnsNotFoundOnEmptyAndCompletedBoards() {
        checks.assertNotFound(empty)
        checks.assertNotFound(LogicalState.from(TestBoards.solved()))
    }

    @Test
    fun doesNotPlaceADigitWithoutCandidatesOrWithMultipleCandidates() {
        checks.assertNotFound(
            LogicalStates.withCandidates(
                empty,
                0 to emptySet(),
                1 to setOf(2, 3)
            )
        )
        checks.assertNotFound(empty.copy(candidates = List(SudokuBoard.CELL_COUNT) { emptySet() }))
    }

    @Test
    fun returnsTheFirstSingleInReadingOrderRatherThanDigitOrder() {
        val state: LogicalState = LogicalStates.withCandidates(empty, 0 to setOf(9), 80 to setOf(1))
        checks.assertPlacement(state, 0, 9)
    }

    @Test
    fun continuesPastCellsWithoutCandidatesAndWithMultipleCandidates() {
        val state: LogicalState =
            LogicalStates.withCandidates(empty, 0 to emptySet(), 1 to setOf(2, 3), 2 to setOf(4))
        checks.assertPlacement(state, 2, 4)
    }

    @Test
    fun doesNotRestoreAnEliminatedSingle() {
        val initial: LogicalState = LogicalStates.withCandidates(empty, 0 to setOf(5), 80 to setOf(2))
        val state: LogicalState = LogicalStates.withCandidates(initial, 0 to emptySet())
        checks.assertPlacement(state, 80, 2)
    }

    @Test
    fun skipsFilledCellsAndFindsTheRemainingSingleOnARealBoard() {
        val solution: SudokuBoard = TestBoards.solved()
        val state: LogicalState = LogicalState.from(solution.withValue(40, 0))
        checks.assertPlacement(state, 40, solution[40])
    }
}
