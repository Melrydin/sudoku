package sudoku.domain.grading.techniques

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueSearchResult

class LockedCandidatesPointingDetectorTest {
    private val detector = LockedCandidatesPointingDetector()

    @Test
    fun findsPairsAndTriplesForEveryDigitInEveryBoxAndLine() {
        SudokuUnits.boxes.forEach { box ->
            (SudokuUnits.rows + SudokuUnits.columns).forEach { line -> checkPatterns(box, line) }
        }
    }

    @Test
    fun ignoresCellsWithoutTheCandidateAndDoesNotChangeTheInput() {
        val box = SudokuUnits.boxes.first()
        val line = SudokuUnits.rows.first()
        val state = pattern(box, listOf(0, 1), 5, excluded = setOf(3, 4))
        val before = state.candidates.map { it.toSet() }
        assertEliminations(state, (line - box.toSet() - setOf(3, 4)).toSet(), 5)
        assertEquals(before, state.candidates)
    }

    @Test
    fun returnsNotFoundWithoutAnAlignedPatternOrEffectiveEliminations() {
        val box = SudokuUnits.boxes.first()
        val outside = (SudokuUnits.rows.first() - box.toSet()).toSet()
        assertEquals(TechniqueSearchResult.NotFound, detector.findStep(LogicalState.from(SudokuBoard.empty())))
        assertEquals(TechniqueSearchResult.NotFound, detector.findStep(pattern(box, listOf(0, 10, 20), 5)))
        assertEquals(TechniqueSearchResult.NotFound, detector.findStep(pattern(box, listOf(0, 1), 5, outside)))
    }

    @Test
    fun continuesAfterAnIneffectiveDigitAndReturnsTheFirstEffectiveDigit() {
        val box = SudokuUnits.boxes.first()
        val outside = (SudokuUnits.rows.first() - box.toSet()).toSet()
        val ineffective = pattern(box, listOf(0, 1), 1, outside)
        val state = restrict(ineffective, box, listOf(0, 1), 5, emptySet())
        assertEliminations(state, outside, 5)
    }

    @Test
    fun keepsColumnPriorityWhenASingleCandidateAlsoSharesARow() {
        val state = pattern(SudokuUnits.boxes.first(), listOf(0), 5)
        assertEliminations(state, setOf(27, 36, 45, 54, 63, 72), 5)
    }

    @Test
    fun doesNotFallBackToTheRowAfterAnIneffectiveSingletonColumn() {
        val outside = setOf(27, 36, 45, 54, 63, 72)
        val state = pattern(SudokuUnits.boxes.first(), listOf(0), 5, outside)
        assertEquals(TechniqueSearchResult.NotFound, detector.findStep(state))
    }

    private fun checkPatterns(box: List<Int>, line: List<Int>) {
        val cells = line.filter { it in box }
        if (cells.size != SudokuBoard.BOX_SIZE) return
        for (count in 2..SudokuBoard.BOX_SIZE) {
            for (digit in 1..SudokuBoard.SIZE) {
                val state = pattern(box, cells.take(count), digit)
                assertEliminations(state, (line - box.toSet()).toSet(), digit)
            }
        }
    }

    private fun assertEliminations(state: LogicalState, cells: Set<Int>, digit: Int) {
        val result = detector.findStep(state)
        assertTrue(result is TechniqueSearchResult.Found)
        val step = (result as TechniqueSearchResult.Found).step
        assertEquals(SolvingTechnique.LOCKED_CANDIDATES_POINTING, step.technique)
        assertEquals(cells.associateWith { setOf(digit) }, step.eliminations)
        assertTrue(step.placements.isEmpty())
        assertTrue(step.isApplicableTo(state))
    }

    private fun pattern(
        box: List<Int>,
        matching: List<Int>,
        digit: Int,
        excluded: Set<Int> = emptySet(),
    ): LogicalState = restrict(LogicalState.from(SudokuBoard.empty()), box, matching, digit, excluded)

    private fun restrict(
        state: LogicalState,
        box: List<Int>,
        matching: List<Int>,
        digit: Int,
        excluded: Set<Int>,
    ): LogicalState = state.copy(candidates = state.candidates.mapIndexed { cell, values ->
        if (cell in excluded || cell in box && cell !in matching) values - digit else values
    })
}
