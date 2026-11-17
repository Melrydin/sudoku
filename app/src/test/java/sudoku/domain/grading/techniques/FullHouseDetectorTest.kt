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
    private val checks: TechniqueAssertions = TechniqueAssertions(FullHouseDetector(), SolvingTechnique.FULL_HOUSE)

    @Test
    fun fillsEveryPositionInEveryRowColumnAndBox() {
        val solution: SudokuBoard = TestBoards.solved()
        SudokuUnits.all.forEach { unit: List<Int> ->
            unit.forEach { cell: Int ->
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
        val values: Map<Int, Int> = mapOf(1 to 1, 2 to 2, 3 to 3, 4 to 4, 9 to 5, 18 to 6, 27 to 7, 36 to 8)
        val board: SudokuBoard = SudokuBoard.from(List(SudokuBoard.CELL_COUNT) { values[it] ?: 0 })
        val state: LogicalState = LogicalState.from(board)
        assertEquals(setOf(9), state.candidates[0])
        checks.assertNotFound(state)
    }

    @Test
    fun rejectsDuplicateValuesEvenWhenTheEmptyCellHasOneCandidate() {
        SudokuUnits.all.forEach { unit: List<Int> ->
            val cell: Int = unit.first()
            val original: LogicalState = LogicalStates.fullHouse(unit to cell)
            val board: SudokuBoard = original.board.withValue(unit[2], original.board[unit[1]])
            val state: LogicalState = LogicalStates.withCandidates(
                LogicalState.from(board),
                cell to original.candidates[cell]
            )
            checks.assertNotFound(state)
        }
    }

    @Test
    fun doesNotRestoreAnEliminatedCandidate() {
        val state: LogicalState = LogicalStates.fullHouse(SudokuUnits.rows.first() to 0)
        checks.assertNotFound(LogicalStates.withCandidates(state, 0 to emptySet()))
    }

    @Test
    fun continuesAfterAUnitWhoseLastCandidateWasEliminated() {
        val state: LogicalState = LogicalStates.fullHouse(SudokuUnits.rows[0] to 0, SudokuUnits.rows[1] to 9)
        val restricted: LogicalState = LogicalStates.withCandidates(state, 0 to emptySet())
        checks.assertPlacement(restricted, 9, TestBoards.solved()[9])
    }

    @Test
    fun searchesRowsBeforeColumns() {
        val state: LogicalState =
            LogicalStates.fullHouse(SudokuUnits.rows.last() to 80, SudokuUnits.columns.first() to 0)
        checks.assertPlacement(state, 80, TestBoards.solved()[80])
    }

    @Test
    fun searchesColumnsBeforeBoxes() {
        val state: LogicalState = LogicalStates.fullHouse(
            SudokuUnits.columns.last() to 80,
            SudokuUnits.boxes.first() to 0
        )
        checks.assertPlacement(state, 80, TestBoards.solved()[80])
    }
}
