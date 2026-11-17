package sudoku.domain.grading.techniques

import org.junit.Assert.assertEquals
import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.fixtures.LogicalStates
import sudoku.fixtures.TestBoards
import sudoku.testing.TechniqueAssertions

class HiddenSingleDetectorTest {
    private val checks: TechniqueAssertions =
        TechniqueAssertions(HiddenSingleDetector(), SolvingTechnique.HIDDEN_SINGLE)
    private val empty: LogicalState = LogicalState.from(SudokuBoard.empty())

    @Test
    fun findsEveryDigitAtEveryPositionInEveryRowColumnAndBox() {
        SudokuUnits.all.forEach { unit: List<Int> ->
            unit.forEach { cell: Int ->
                for (digit: Int in 1..SudokuBoard.SIZE) {
                    val state: LogicalState = LogicalStates.restrictDigit(empty, unit, listOf(cell), digit)
                    assertEquals(SudokuBoard.SIZE, state.candidates[cell].size)
                    checks.assertPlacement(state, cell, digit)
                }
            }
        }
    }

    @Test
    fun returnsNotFoundWithoutAUniqueCandidatePositionOrOnACompletedBoard() {
        checks.assertNotFound(empty)
        checks.assertNotFound(LogicalState.from(TestBoards.solved()))
    }

    @Test
    fun doesNotPlaceADigitWithTwoPossiblePositionsInAUnit() {
        SudokuUnits.all.forEach { unit: List<Int> ->
            checks.assertNotFound(LogicalStates.restrictDigit(empty, unit, unit.take(2), 5))
        }
    }

    @Test
    fun doesNotPlaceADigitThatHasBeenEliminatedFromTheWholeUnit() {
        SudokuUnits.all.forEach { unit: List<Int> ->
            checks.assertNotFound(LogicalStates.restrictDigit(empty, unit, emptyList(), 5))
        }
    }

    @Test
    fun continuesPastAUnitWithNoPositionsForADigit() {
        val restricted: LogicalState =
            LogicalStates.restrictDigit(empty, SudokuUnits.rows.first(), emptyList(), 1)
        val state: LogicalState = LogicalStates.restrictDigit(restricted, SudokuUnits.rows[1], listOf(9), 5)
        checks.assertPlacement(state, 9, 5)
    }

    @Test
    fun searchesDigitsInAscendingOrderWithinAUnit() {
        val laterDigit: LogicalState = LogicalStates.restrictDigit(empty, SudokuUnits.rows.first(), listOf(0), 9)
        val state: LogicalState = LogicalStates.restrictDigit(laterDigit, SudokuUnits.rows.first(), listOf(8), 2)
        checks.assertPlacement(state, 8, 2)
    }

    @Test
    fun searchesRowsBeforeColumns() {
        val row: LogicalState = LogicalStates.restrictDigit(empty, SudokuUnits.rows.last(), listOf(80), 5)
        val state: LogicalState = LogicalStates.restrictDigit(row, SudokuUnits.columns.first(), listOf(0), 2)
        checks.assertPlacement(state, 80, 5)
    }

    @Test
    fun searchesColumnsBeforeBoxes() {
        val box: LogicalState = LogicalStates.restrictDigit(empty, SudokuUnits.boxes.first(), listOf(0), 2)
        val state: LogicalState = LogicalStates.restrictDigit(box, SudokuUnits.columns.last(), listOf(80), 5)
        checks.assertPlacement(state, 80, 5)
    }
}
