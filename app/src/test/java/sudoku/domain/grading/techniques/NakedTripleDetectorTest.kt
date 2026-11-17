package sudoku.domain.grading.techniques

import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.fixtures.LogicalStates
import sudoku.fixtures.TestBoards
import sudoku.testing.TechniqueAssertions

class NakedTripleDetectorTest {
    private val assertions: TechniqueAssertions =
        TechniqueAssertions(NakedTripleDetector(), SolvingTechnique.NAKED_TRIPLE)
    private val emptyState: LogicalState = LogicalState.from(SudokuBoard.empty())

    @Test
    fun findsEveryDigitTripleInEveryRowColumnAndBox() {
        SudokuUnits.all.forEach(::checkEveryDigitTriple)
    }

    @Test
    fun acceptsAllTwoAndThreeCandidatePatternsInEveryUnit() {
        val digits: Set<Int> = setOf(2, 7, 9)
        val patterns: List<List<Set<Int>>> = candidatePatterns(digits)
        for (unit: List<Int> in SudokuUnits.all) {
            for (pattern: List<Set<Int>> in patterns) {
                val changes: List<Pair<Int, Set<Int>>> = tripleCells(unit).zip(pattern)
                val state: LogicalState = stateWithCandidates(*changes.toTypedArray())
                assertUnitEliminations(unit, state, digits)
            }
        }
    }

    @Test
    fun removesOnlyPresentTripleDigitsAndIgnoresCandidateOrder() {
        val state: LogicalState = stateWithCandidates(
            0 to linkedSetOf(7, 2), 4 to linkedSetOf(9, 2), 8 to linkedSetOf(9, 7),
            1 to setOf(1, 2, 7, 9), 2 to setOf(3, 7, 8), 3 to setOf(1, 4, 6),
            5 to setOf(2, 3, 9), 6 to setOf(1), 7 to setOf(1, 3, 4, 5),
        )
        val expected: Map<Int, Set<Int>> = mapOf(1 to setOf(2, 7, 9), 2 to setOf(7), 5 to setOf(2, 9))
        assertions.assertEliminations(state, expected)
    }

    @Test
    fun returnsNotFoundOnAnEmptyOrCompletedBoard() {
        assertions.assertNotFound(emptyState)
        assertions.assertNotFound(LogicalState.from(TestBoards.solved()))
    }

    @Test
    fun requiresThreeCellsWithTwoOrThreeCandidates() {
        assertions.assertNotFound(stateWithCandidates(0 to setOf(2, 7), 4 to setOf(2, 9)))
        assertions.assertNotFound(stateWithCandidates(0 to setOf(2), 4 to setOf(2, 7), 8 to setOf(7, 9)))
        val tooMany: LogicalState = stateWithCandidates(
            0 to setOf(2, 7), 4 to setOf(2, 9), 8 to setOf(1, 2, 7, 9),
        )
        assertions.assertNotFound(tooMany)
    }

    @Test
    fun rejectsACombinedCandidateSetWithOnlyTwoDigits() {
        val state: LogicalState = stateWithCandidates(0 to setOf(2, 7), 4 to setOf(2, 7), 8 to setOf(2, 7))
        assertions.assertNotFound(state)
        assertions.assertNotFound(LogicalState(SudokuBoard.empty(), List(81) { setOf(2, 7) }))
    }

    @Test
    fun doesNotMistakeANakedQuadForATriple() {
        val state: LogicalState = stateWithCandidates(
            0 to setOf(1, 2), 3 to setOf(2, 3), 6 to setOf(3, 4), 8 to setOf(1, 4),
        )
        assertions.assertNotFound(state)
    }

    @Test
    fun requiresAllThreeCellsToShareAUnit() {
        val state: LogicalState = stateWithCandidates(0 to setOf(2, 7), 40 to setOf(2, 9), 80 to setOf(7, 9))
        assertions.assertNotFound(state)
    }

    @Test
    fun returnsNotFoundWithoutEffectiveEliminations() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, listOf(2, 7, 9))
        assertions.assertNotFound(withoutTripleDigitsOutside(initial, tripleCells(unit), setOf(2, 7, 9)))
    }

    @Test
    fun continuesAfterAnIneffectiveCombinationInTheSameUnit() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, listOf(1, 2, 3))
        val ineffective: LogicalState = withoutTripleDigitsOutside(initial, tripleCells(unit), setOf(1, 2, 3))
        val state: LogicalState = LogicalStates.withCandidates(
            ineffective, 1 to setOf(4, 5), 3 to setOf(4, 6), 5 to setOf(5, 6),
        )
        assertions.assertEliminations(state, listOf(2, 6, 7).associateWith { setOf(4, 5, 6) })
    }

    @Test
    fun continuesAfterAnIneffectiveUnit() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, listOf(1, 2, 3))
        val ineffective: LogicalState = withoutTripleDigitsOutside(initial, tripleCells(unit), setOf(1, 2, 3))
        val state: LogicalState = LogicalStates.withCandidates(
            ineffective, 27 to setOf(4, 5), 31 to setOf(4, 6), 35 to setOf(5, 6),
        )
        assertUnitEliminations(SudokuUnits.rows[3], state, setOf(4, 5, 6))
    }

    @Test
    fun searchesPastInvalidCombinationsAndFindsTheLastOne() {
        val state: LogicalState = stateWithCandidates(
            0 to setOf(1, 3), 1 to setOf(3, 4), 2 to setOf(4, 5),
            3 to setOf(5, 6), 4 to setOf(6, 1),
            5 to setOf(2, 7), 6 to setOf(2, 9), 7 to setOf(7, 9),
        )
        assertions.assertEliminations(state, mapOf(8 to setOf(2, 7, 9)))
    }

    @Test
    fun returnsOnlyTheFirstEffectiveCombinationInReadingOrder() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(2, 7, 9))
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 1 to setOf(1, 3), 3 to setOf(1, 5), 5 to setOf(3, 5),
        )
        assertions.assertEliminations(state, listOf(2, 6, 7).associateWith { setOf(2, 7, 9) })
    }

    @Test
    fun searchesRowsBeforeColumns() {
        val initial: LogicalState = pattern(SudokuUnits.rows.last(), listOf(2, 7, 9))
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 1 to setOf(1, 3), 28 to setOf(1, 5), 55 to setOf(3, 5),
        )
        assertUnitEliminations(SudokuUnits.rows.last(), state, setOf(2, 7, 9))
    }

    @Test
    fun searchesColumnsBeforeBoxes() {
        val initial: LogicalState = pattern(SudokuUnits.columns.last(), listOf(2, 7, 9))
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(1, 3), 10 to setOf(1, 5), 20 to setOf(3, 5),
        )
        assertUnitEliminations(SudokuUnits.columns.last(), state, setOf(2, 7, 9))
    }

    @Test
    fun neverEliminatesFromFilledCells() {
        val initial: LogicalState = LogicalState.from(SudokuBoard.empty().withValue(1, 1))
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(2, 7), 4 to setOf(2, 9), 8 to setOf(7, 9),
        )
        assertions.assertEliminations(state, listOf(2, 3, 5, 6, 7).associateWith { setOf(2, 7, 9) })
    }

    private fun checkEveryDigitTriple(unit: List<Int>) {
        for (first: Int in 1..7) {
            for (second: Int in (first + 1)..8) {
                for (third: Int in (second + 1)..9) {
                    val digits: List<Int> = listOf(first, second, third)
                    assertUnitEliminations(unit, pattern(unit, digits), digits.toSet())
                }
            }
        }
    }

    private fun candidatePatterns(digits: Set<Int>): List<List<Set<Int>>> {
        val options: List<Set<Int>> = digits.map { omitted: Int -> digits - omitted } + listOf(digits)
        val patterns: MutableList<List<Set<Int>>> = mutableListOf()
        for (first: Set<Int> in options) {
            for (second: Set<Int> in options) {
                for (third: Set<Int> in options) {
                    val pattern: List<Set<Int>> = listOf(first, second, third)
                    if (pattern.flatten().toSet().size == 3) patterns.add(pattern)
                }
            }
        }
        return patterns
    }

    private fun pattern(unit: List<Int>, digits: List<Int>): LogicalState {
        val cells: List<Int> = tripleCells(unit)
        return stateWithCandidates(
            cells[0] to setOf(digits[0], digits[1]),
            cells[1] to setOf(digits[0], digits[2]),
            cells[2] to setOf(digits[1], digits[2]),
        )
    }

    private fun assertUnitEliminations(unit: List<Int>, state: LogicalState, digits: Set<Int>) {
        val targets: List<Int> = unit - tripleCells(unit).toSet()
        assertions.assertEliminations(state, targets.associateWith { digits })
    }

    private fun tripleCells(unit: List<Int>): List<Int> = listOf(unit.first(), unit[4], unit.last())

    private fun stateWithCandidates(vararg changes: Pair<Int, Set<Int>>): LogicalState =
        LogicalStates.withCandidates(emptyState, *changes)

    private fun withoutTripleDigitsOutside(
        state: LogicalState,
        cells: List<Int>,
        digits: Set<Int>,
    ): LogicalState = state.copy(candidates = state.candidates.mapIndexed { cell: Int, candidates: Set<Int> ->
        if (cell in cells) candidates else candidates - digits
    })
}
