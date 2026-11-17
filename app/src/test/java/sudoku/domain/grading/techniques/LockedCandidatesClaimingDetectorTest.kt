package sudoku.domain.grading.techniques

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.LogicalStep
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueSearchResult
import sudoku.fixtures.TestBoards

class LockedCandidatesClaimingDetectorTest {
    private val detector: LockedCandidatesClaimingDetector = LockedCandidatesClaimingDetector()

    @Test
    fun findsAllPairsAndTriplesForEveryDigitInEveryBoxAndLine() {
        SudokuUnits.boxes.forEach { box: List<Int> ->
            (SudokuUnits.rows + SudokuUnits.columns).forEach { line: List<Int> -> checkPatterns(box, line) }
        }
    }

    @Test
    fun ignoresCellsWithoutTheCandidateAndDoesNotChangeTheInput() {
        val line: List<Int> = SudokuUnits.rows.first()
        val state: LogicalState = pattern(line, listOf(0, 1), 5, excluded = setOf(9, 19))
        val before: List<Set<Int>> = state.candidates.map { it.toSet() }
        val boardBefore: List<Int> = state.board.cells.toList()
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
        val line: List<Int> = SudokuUnits.rows.first()
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
        val box: List<Int> = SudokuUnits.boxes.first()
        val row: List<Int> = SudokuUnits.rows.first()
        val column: List<Int> = SudokuUnits.columns.first()
        assertNotFound(pattern(row, listOf(0, 1), 5, (box - row.toSet()).toSet()))
        assertNotFound(pattern(column, listOf(0, 9), 5, (box - column.toSet()).toSet()))
    }

    @Test
    fun continuesAfterAnIneffectiveDigitAndReturnsTheFirstEffectiveDigit() {
        val line: List<Int> = SudokuUnits.rows.first()
        val outside: Set<Int> = (SudokuUnits.boxes.first() - line.toSet()).toSet()
        val ineffective: LogicalState = pattern(line, listOf(0, 1), 1, outside)
        val state: LogicalState = restrict(ineffective, line, listOf(0, 1), 5)
        assertEliminations(state, outside, 5)
    }

    @Test
    fun continuesAfterAnIneffectiveLine() {
        val first: List<Int> = SudokuUnits.rows.first()
        val outside: Set<Int> = (SudokuUnits.boxes.first() - first.toSet()).toSet()
        val ineffective: LogicalState = pattern(first, listOf(0, 1), 5, outside)
        val state: LogicalState = restrict(ineffective, SudokuUnits.rows[3], listOf(27, 28), 5)
        assertEliminations(state, setOf(36, 37, 38, 45, 46, 47), 5)
    }

    @Test
    fun searchesRowsBeforeColumns() {
        val rowPattern: LogicalState = pattern(SudokuUnits.rows.last(), listOf(78, 79), 5)
        val state: LogicalState = restrict(rowPattern, SudokuUnits.columns.first(), listOf(0, 9), 2)
        assertEliminations(state, setOf(60, 61, 62, 69, 70, 71), 5)
    }

    @Test
    fun returnsTheFirstEffectiveDigitWhenSeveralDigitsAllowClaiming() {
        val line: List<Int> = SudokuUnits.rows.first()
        val laterDigit: LogicalState = pattern(line, listOf(0, 1), 9)
        val state: LogicalState = restrict(laterDigit, line, listOf(0, 1), 2)
        assertEliminations(state, setOf(9, 10, 11, 18, 19, 20), 2)
    }

    @Test
    fun neverEliminatesFromFilledCells() {
        val initial: LogicalState = LogicalState.from(SudokuBoard.empty().withValue(9, 1))
        val state: LogicalState = restrict(initial, SudokuUnits.rows.first(), listOf(0, 1), 5)
        assertEliminations(state, setOf(10, 11, 18, 19, 20), 5)
        assertEquals(1, state.board[9])
        assertTrue(state.candidates[9].isEmpty())
    }

    private fun checkPatterns(box: List<Int>, line: List<Int>) {
        val cells: List<Int> = line.filter { it in box }
        if (cells.size != SudokuBoard.BOX_SIZE) return
        val groups: List<List<Int>> =
            listOf(cells.take(2), cells.takeLast(2), listOf(cells.first(), cells.last()), cells)
        for (matching: List<Int> in groups) {
            for (digit: Int in 1..SudokuBoard.SIZE) {
                assertEliminations(
                    pattern(line, matching, digit),
                    (box - line.toSet()).toSet(),
                    digit
                )
            }
        }
    }

    private fun assertEliminations(state: LogicalState, cells: Set<Int>, digit: Int) {
        val result: TechniqueSearchResult = detector.findStep(state)
        assertTrue(result is TechniqueSearchResult.Found)
        val step: LogicalStep = (result as TechniqueSearchResult.Found).step
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
    ): LogicalState = state.copy(candidates = state.candidates.mapIndexed { cell: Int, values: Set<Int> ->
        if (cell in excluded || cell in line && cell !in matching) values - digit else values
    })
}
