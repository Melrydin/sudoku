package sudoku.domain.grading.techniques

import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.fixtures.LogicalStates
import sudoku.fixtures.TestBoards
import sudoku.testing.TechniqueAssertions

class XWingDetectorTest {
    private val assertions: TechniqueAssertions = TechniqueAssertions(XWingDetector(), SolvingTechnique.X_WING)
    private val emptyState: LogicalState = LogicalState.from(SudokuBoard.empty())
    private val rowTargets: List<Int> = listOf(1, 6, 19, 24, 28, 33, 46, 51, 55, 60, 64, 69, 73, 78)

    @Test
    fun eliminatesFromColumnsForARowXWing() {
        val state: LogicalState = pattern(SudokuUnits.rows, listOf(1, 4), listOf(1, 6))
        assertEliminations(state, rowTargets)
    }

    @Test
    fun eliminatesFromRowsForAColumnXWing() {
        val state: LogicalState = pattern(SudokuUnits.columns, listOf(1, 6), listOf(1, 4))
        assertEliminations(state, listOf(9, 11, 12, 13, 14, 16, 17, 36, 38, 39, 40, 41, 43, 44))
    }

    @Test
    fun removesOnlyExistingCandidatesAndPreservesTheCorners() {
        val initial: LogicalState = pattern(SudokuUnits.rows, listOf(1, 4), listOf(1, 6))
        val state: LogicalState = LogicalStates.restrictDigit(
            initial, (0..80).toList(), listOf(10, 15, 28, 37, 42), 5,
        )
        assertEliminations(state, listOf(28))
    }

    @Test
    fun rejectsDifferentCandidatePositionsInBothOrientations() {
        for (units: List<List<Int>> in listOf(SudokuUnits.rows, SudokuUnits.columns)) {
            val first: LogicalState = restrictUnit(emptyState, units[1], listOf(1, 6))
            val state: LogicalState = restrictUnit(first, units[4], listOf(1, 7))
            assertions.assertNotFound(state)
        }
    }

    @Test
    fun rejectsAThirdCandidatePositionInBothOrientations() {
        for (units: List<List<Int>> in listOf(SudokuUnits.rows, SudokuUnits.columns)) {
            val first: LogicalState = restrictUnit(emptyState, units[1], listOf(1, 6))
            val state: LogicalState = restrictUnit(first, units[4], listOf(1, 6, 7))
            assertions.assertNotFound(state)
        }
    }

    @Test
    fun returnsNotFoundWithoutEffectiveEliminations() {
        val initial: LogicalState = pattern(SudokuUnits.rows, listOf(1, 4), listOf(1, 6))
        val state: LogicalState = LogicalStates.restrictDigit(
            initial, (0..80).toList(), listOf(10, 15, 37, 42), 5,
        )
        assertions.assertNotFound(state)
    }

    @Test
    fun continuesAfterAnIneffectivePattern() {
        val initial: LogicalState = pattern(SudokuUnits.rows, listOf(1, 4), listOf(1, 6))
        val ineffective: LogicalState = LogicalStates.restrictDigit(
            initial, SudokuUnits.columns[1] + SudokuUnits.columns[6], listOf(10, 15, 37, 42), 5,
        )
        val state: LogicalState = pattern(SudokuUnits.rows, listOf(2, 5), listOf(3, 8), ineffective)
        assertEliminations(state, listOf(3, 8, 30, 35, 57, 62, 66, 71, 75, 80))
    }

    @Test
    fun neverEliminatesFromFilledCells() {
        val initial: LogicalState = LogicalState.from(SudokuBoard.empty().withValue(28, 1))
        val state: LogicalState = pattern(SudokuUnits.rows, listOf(1, 4), listOf(1, 6), initial)
        assertEliminations(state, rowTargets - 28)
    }

    @Test
    fun returnsNotFoundOnAnEmptyOrCompletedBoard() {
        assertions.assertNotFound(emptyState)
        assertions.assertNotFound(LogicalState.from(TestBoards.solved()))
    }

    private fun pattern(
        units: List<List<Int>>,
        indices: List<Int>,
        positions: List<Int>,
        state: LogicalState = emptyState,
    ): LogicalState = indices.fold(state) { restricted: LogicalState, index: Int ->
        restrictUnit(restricted, units[index], positions)
    }

    private fun restrictUnit(state: LogicalState, unit: List<Int>, positions: List<Int>): LogicalState {
        val cells: List<Int> = positions.map { position: Int -> unit[position] }
        return LogicalStates.restrictDigit(state, unit, cells, 5)
    }

    private fun assertEliminations(state: LogicalState, cells: List<Int>) {
        assertions.assertEliminations(state, cells.associateWith { setOf(5) })
    }
}
