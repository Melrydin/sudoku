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

class FullHouseDetectorTest {
    private val checks = TechniqueAssertions(FullHouseDetector(), SolvingTechnique.FULL_HOUSE)

    @Test
    fun fillsEveryPositionInEveryRowColumnAndBox() {
        val solution = TestBoards.solved()
        SudokuUnits.all.forEach { unit ->
            unit.forEach { cell ->
                checks.assertPlacement(LogicalStates.fullHouse(unit to cell), cell, solution[cell])
            }
        }
    }

    @Test
    fun returnsNotFoundOnEmptyAndCompletedBoards() {
        checks.assertNotFound(LogicalState.from(SudokuBoard.empty()))
        checks.assertNotFound(LogicalState.from(TestBoards.solved()))
    }

    @Test
    fun doesNotTreatANakedSingleAsAFullHouse() {
        val values = mapOf(1 to 1, 2 to 2, 3 to 3, 4 to 4, 9 to 5, 18 to 6, 27 to 7, 36 to 8)
        val board = SudokuBoard.from(List(SudokuBoard.CELL_COUNT) { values[it] ?: 0 })
        val state = LogicalState.from(board)
        assertEquals(setOf(9), state.candidates[0])
        checks.assertNotFound(state)
    }

    @Test
    fun rejectsDuplicateValuesEvenWhenTheEmptyCellHasOneCandidate() {
        SudokuUnits.all.forEach { unit ->
            val cell = unit.first()
            val original = LogicalStates.fullHouse(unit to cell)
            val board = original.board.withValue(unit[2], original.board[unit[1]])
            val state = LogicalStates.withCandidates(LogicalState.from(board), cell to original.candidates[cell])
            checks.assertNotFound(state)
        }
    }

    @Test
    fun doesNotRestoreAnEliminatedCandidate() {
        val state = LogicalStates.fullHouse(SudokuUnits.rows.first() to 0)
        checks.assertNotFound(LogicalStates.withCandidates(state, 0 to emptySet()))
    }

    @Test
    fun continuesAfterAUnitWhoseLastCandidateWasEliminated() {
        val state = LogicalStates.fullHouse(SudokuUnits.rows[0] to 0, SudokuUnits.rows[1] to 9)
        val restricted = LogicalStates.withCandidates(state, 0 to emptySet())
        checks.assertPlacement(restricted, 9, TestBoards.solved()[9])
    }

    @Test
    fun searchesRowsBeforeColumns() {
        val state = LogicalStates.fullHouse(SudokuUnits.rows.last() to 80, SudokuUnits.columns.first() to 0)
        checks.assertPlacement(state, 80, TestBoards.solved()[80])
    }

    @Test
    fun searchesColumnsBeforeBoxes() {
        val state = LogicalStates.fullHouse(SudokuUnits.columns.last() to 80, SudokuUnits.boxes.first() to 0)
        checks.assertPlacement(state, 80, TestBoards.solved()[80])
    }
}
