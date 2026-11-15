package sudoku.domain.grading.techniques

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueSearchResult
import sudoku.fixtures.TestBoards

class LockedCandidatesClaimingDetectorTest {
    private val detector = LockedCandidatesClaimingDetector()

    @Test
    fun findsAllPairsAndTriplesForEveryDigitInEveryBoxAndLine() {
        SudokuUnits.boxes.forEach { box ->
            (SudokuUnits.rows + SudokuUnits.columns).forEach { line -> checkPatterns(box, line) }
        }
    }

    @Test
    fun ignoresCellsWithoutTheCandidateAndDoesNotChangeTheInput() {
        val line = SudokuUnits.rows.first()
        val state = pattern(line, listOf(0, 1), 5, excluded = setOf(9, 19))
        val before = state.candidates.map { it.toSet() }
        val boardBefore = state.board.cells.toList()
        assertEliminations(state, setOf(10, 11, 18, 20), 5)
        assertEquals(before, state.candidates)
        assertEquals(boardBefore, state.board.cells)
    }

    @Test
    fun returnsNotFoundWithoutRestrictedCandidatesOrOnACompletedBoard() {
        assertNotFound(LogicalState.from(SudokuBoard.empty()))
        assertNotFound(LogicalState.from(TestBoards.solved()))
    }

    @Test
    fun skipsLinesWithoutCandidatesAndLeavesSinglesToOtherTechniques() {
        val line = SudokuUnits.rows.first()
        assertNotFound(pattern(line, emptyList(), 5))
        assertNotFound(pattern(line, listOf(0), 5))
    }

    @Test
    fun returnsNotFoundWhenCandidatesSpanMultipleBoxes() {
        assertNotFound(pattern(SudokuUnits.rows.first(), listOf(0, 3), 5))
        assertNotFound(pattern(SudokuUnits.columns.first(), listOf(0, 27), 5))
    }

    @Test
    fun returnsNotFoundWhenTheSharedBoxHasNoEffectiveEliminations() {
        val box = SudokuUnits.boxes.first()
        val row = SudokuUnits.rows.first()
        val column = SudokuUnits.columns.first()
        assertNotFound(pattern(row, listOf(0, 1), 5, (box - row.toSet()).toSet()))
        assertNotFound(pattern(column, listOf(0, 9), 5, (box - column.toSet()).toSet()))
    }

    @Test
    fun continuesAfterAnIneffectiveDigitAndReturnsTheFirstEffectiveDigit() {
        val line = SudokuUnits.rows.first()
        val outside = (SudokuUnits.boxes.first() - line.toSet()).toSet()
        val ineffective = pattern(line, listOf(0, 1), 1, outside)
        val state = restrict(ineffective, line, listOf(0, 1), 5)
        assertEliminations(state, outside, 5)
    }

    @Test
    fun continuesAfterAnIneffectiveLine() {
        val first = SudokuUnits.rows.first()
        val outside = (SudokuUnits.boxes.first() - first.toSet()).toSet()
        val ineffective = pattern(first, listOf(0, 1), 5, outside)
        val state = restrict(ineffective, SudokuUnits.rows[3], listOf(27, 28), 5)
        assertEliminations(state, setOf(36, 37, 38, 45, 46, 47), 5)
    }

    @Test
    fun searchesRowsBeforeColumns() {
        val rowPattern = pattern(SudokuUnits.rows.last(), listOf(78, 79), 5)
        val state = restrict(rowPattern, SudokuUnits.columns.first(), listOf(0, 9), 2)
        assertEliminations(state, setOf(60, 61, 62, 69, 70, 71), 5)
    }

    @Test
    fun returnsTheFirstEffectiveDigitWhenSeveralDigitsAllowClaiming() {
        val line = SudokuUnits.rows.first()
        val laterDigit = pattern(line, listOf(0, 1), 9)
        val state = restrict(laterDigit, line, listOf(0, 1), 2)
        assertEliminations(state, setOf(9, 10, 11, 18, 19, 20), 2)
    }

    @Test
    fun neverEliminatesFromFilledCells() {
        val initial = LogicalState.from(SudokuBoard.empty().withValue(9, 1))
        val state = restrict(initial, SudokuUnits.rows.first(), listOf(0, 1), 5)
        assertEliminations(state, setOf(10, 11, 18, 19, 20), 5)
        assertEquals(1, state.board[9])
        assertTrue(state.candidates[9].isEmpty())
    }

    private fun checkPatterns(box: List<Int>, line: List<Int>) {
        val cells = line.filter { it in box }
        if (cells.size != SudokuBoard.BOX_SIZE) return
        val groups =
            listOf(cells.take(2), cells.takeLast(2), listOf(cells.first(), cells.last()), cells)
        for (matching in groups) {
            for (digit in 1..SudokuBoard.SIZE) {
                assertEliminations(
                    pattern(line, matching, digit),
                    (box - line.toSet()).toSet(),
                    digit
                )
            }
        }
    }

    private fun assertEliminations(state: LogicalState, cells: Set<Int>, digit: Int) {
        val result = detector.findStep(state)
        assertTrue(result is TechniqueSearchResult.Found)
        val step = (result as TechniqueSearchResult.Found).step
        assertEquals(SolvingTechnique.LOCKED_CANDIDATES_CLAIMING, step.technique)
        assertEquals(cells.associateWith { setOf(digit) }, step.eliminations)
        assertTrue(step.placements.isEmpty())
        assertTrue(step.isApplicableTo(state))
    }

    private fun assertNotFound(state: LogicalState) {
        assertEquals(TechniqueSearchResult.NotFound, detector.findStep(state))
    }

    private fun pattern(
        line: List<Int>,
        matching: List<Int>,
        digit: Int,
        excluded: Set<Int> = emptySet(),
    ): LogicalState =
        restrict(LogicalState.from(SudokuBoard.empty()), line, matching, digit, excluded)

    private fun restrict(
        state: LogicalState,
        line: List<Int>,
        matching: List<Int>,
        digit: Int,
        excluded: Set<Int> = emptySet(),
    ): LogicalState = state.copy(candidates = state.candidates.mapIndexed { cell, values ->
        if (cell in excluded || cell in line && cell !in matching) values - digit else values
    })
}
