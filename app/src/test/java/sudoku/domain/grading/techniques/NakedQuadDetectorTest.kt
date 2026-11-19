package sudoku.domain.grading.techniques

import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.fixtures.LogicalStates
import sudoku.fixtures.TestBoards
import sudoku.testing.TechniqueAssertions

class NakedQuadDetectorTest {
    private val assertions: TechniqueAssertions =
        TechniqueAssertions(NakedQuadDetector(), SolvingTechnique.NAKED_QUAD)
    private val emptyState: LogicalState = LogicalState.from(SudokuBoard.empty())
    private val quadDigits: List<Int> = listOf(2, 4, 7, 9)

    @Test
    fun findsEveryDigitQuadInEveryRowColumnAndBox() {
        SudokuUnits.all.forEach(::checkEveryDigitQuad)
    }

    @Test
    fun findsEveryCellQuadInEveryRowColumnAndBox() {
        SudokuUnits.all.forEach(::checkEveryCellQuad)
    }

    @Test
    fun acceptsEveryCombinationOfTwoThreeAndFourCandidateCounts() {
        val patterns: List<List<Set<Int>>> = candidatePatterns()
        for (unit: List<Int> in SudokuUnits.all) {
            for (pattern: List<Set<Int>> in patterns) {
                val changes: List<Pair<Int, Set<Int>>> = quadCells(unit).zip(pattern)
                val state: LogicalState = stateWithCandidates(*changes.toTypedArray())
                assertUnitEliminations(unit, quadCells(unit), state, quadDigits.toSet())
            }
        }
    }

    @Test
    fun removesOnlyPresentQuadDigitsAndIgnoresCandidateOrder() {
        val state: LogicalState = stateWithCandidates(
            0 to linkedSetOf(4, 2), 2 to linkedSetOf(7, 4),
            4 to linkedSetOf(9, 7), 8 to linkedSetOf(2, 9),
            1 to setOf(1, 2, 4, 7, 9), 3 to setOf(1, 3, 4, 5, 6),
            5 to setOf(1, 3, 5, 6, 8), 6 to setOf(1), 7 to setOf(1, 3, 5, 7, 9),
        )
        val expected: Map<Int, Set<Int>> = mapOf(1 to setOf(2, 4, 7, 9), 3 to setOf(4), 7 to setOf(7, 9))
        assertions.assertEliminations(state, expected)
    }

    @Test
    fun returnsNotFoundOnAnEmptyOrCompletedBoard() {
        assertions.assertNotFound(emptyState)
        assertions.assertNotFound(LogicalState.from(TestBoards.solved()))
    }

    @Test
    fun requiresFourDistinctEligibleCells() {
        val unit: List<Int> = SudokuUnits.rows.first()
        for (count: Int in 1..3) {
            val changes: List<Pair<Int, Set<Int>>> = quadCells(unit).take(count).map { cell: Int ->
                cell to quadDigits.toSet()
            }
            assertions.assertNotFound(stateWithCandidates(*changes.toTypedArray()))
        }
    }

    @Test
    fun leavesSingleCandidateCellsToOtherTechniques() {
        val state: LogicalState = stateWithCandidates(
            0 to setOf(2), 2 to setOf(4, 7), 4 to setOf(7, 9), 8 to setOf(2, 9),
        )
        assertions.assertNotFound(state)
    }

    @Test
    fun rejectsACellWithMoreThanFourCandidates() {
        val state: LogicalState = stateWithCandidates(
            0 to setOf(2, 4), 2 to setOf(4, 7), 4 to setOf(7, 9), 8 to setOf(1, 2, 4, 7, 9),
        )
        assertions.assertNotFound(state)
    }

    @Test
    fun rejectsCombinedCandidateSetsWithFewerThanFourDigits() {
        val cells: List<Int> = quadCells(SudokuUnits.rows.first())
        for (digits: Set<Int> in listOf(setOf(2, 4), setOf(2, 4, 7))) {
            val changes: List<Pair<Int, Set<Int>>> = cells.map { cell: Int -> cell to digits }
            assertions.assertNotFound(stateWithCandidates(*changes.toTypedArray()))
        }
    }

    @Test
    fun rejectsACombinedCandidateSetWithMoreThanFourDigits() {
        val state: LogicalState = stateWithCandidates(
            0 to setOf(1, 2), 2 to setOf(2, 3), 4 to setOf(3, 4), 8 to setOf(4, 5),
        )
        assertions.assertNotFound(state)
    }

    @Test
    fun requiresAllFourCellsToShareAUnit() {
        val state: LogicalState = stateWithCandidates(
            0 to setOf(2, 4), 13 to setOf(4, 7), 40 to setOf(7, 9), 80 to setOf(2, 9),
        )
        assertions.assertNotFound(state)
    }

    @Test
    fun returnsNotFoundWithoutEffectiveEliminations() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(quadCells(unit), quadDigits)
        assertions.assertNotFound(withoutQuadDigitsOutside(initial, quadCells(unit), quadDigits.toSet()))
    }

    @Test
    fun continuesAfterAnIneffectiveCombinationInTheSameUnit() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(quadCells(unit), listOf(1, 2, 3, 4))
        val ineffective: LogicalState = withoutQuadDigitsOutside(initial, quadCells(unit), setOf(1, 2, 3, 4))
        val state: LogicalState = LogicalStates.withCandidates(
            ineffective, 1 to setOf(5, 6), 3 to setOf(6, 7), 5 to setOf(7, 8), 7 to setOf(5, 8),
        )
        assertions.assertEliminations(state, mapOf(6 to setOf(5, 6, 7, 8)))
    }

    @Test
    fun continuesAfterAnIneffectiveUnit() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(quadCells(unit), listOf(1, 2, 3, 4))
        val ineffective: LogicalState = withoutQuadDigitsOutside(initial, quadCells(unit), setOf(1, 2, 3, 4))
        val state: LogicalState = LogicalStates.withCandidates(
            ineffective, 27 to setOf(5, 6), 29 to setOf(6, 7), 31 to setOf(7, 8), 35 to setOf(5, 8),
        )
        val laterUnit: List<Int> = SudokuUnits.rows[3]
        assertUnitEliminations(laterUnit, quadCells(laterUnit), state, setOf(5, 6, 7, 8))
    }

    @Test
    fun searchesPastInvalidCombinationsAndFindsTheLastOne() {
        val state: LogicalState = stateWithCandidates(
            0 to setOf(1, 3), 1 to setOf(3, 5), 2 to setOf(5, 6), 3 to setOf(6, 8),
            4 to setOf(2, 4), 5 to setOf(4, 7), 6 to setOf(7, 9), 7 to setOf(2, 9),
        )
        assertions.assertEliminations(state, mapOf(8 to quadDigits.toSet()))
    }

    @Test
    fun returnsOnlyTheFirstEffectiveCombinationInReadingOrder() {
        val initial: LogicalState = pattern(quadCells(SudokuUnits.rows.first()), quadDigits)
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 1 to setOf(1, 3), 3 to setOf(3, 5), 5 to setOf(5, 6), 7 to setOf(1, 6),
        )
        assertions.assertEliminations(state, mapOf(6 to quadDigits.toSet()))
    }

    @Test
    fun searchesRowsBeforeColumns() {
        val unit: List<Int> = SudokuUnits.rows.last()
        val initial: LogicalState = pattern(quadCells(unit), quadDigits)
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 1 to setOf(1, 3), 19 to setOf(3, 5), 37 to setOf(5, 6), 55 to setOf(1, 6),
        )
        assertUnitEliminations(unit, quadCells(unit), state, quadDigits.toSet())
    }

    @Test
    fun searchesColumnsBeforeBoxes() {
        val unit: List<Int> = SudokuUnits.columns.last()
        val initial: LogicalState = pattern(quadCells(unit), quadDigits)
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(1, 3), 2 to setOf(3, 5), 10 to setOf(5, 6), 20 to setOf(1, 6),
        )
        assertUnitEliminations(unit, quadCells(unit), state, quadDigits.toSet())
    }

    @Test
    fun neverEliminatesFromFilledCells() {
        val initial: LogicalState = LogicalState.from(SudokuBoard.empty().withValue(1, 1))
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(2, 4), 2 to setOf(4, 7), 4 to setOf(7, 9), 8 to setOf(2, 9),
        )
        assertions.assertEliminations(state, listOf(3, 5, 6, 7).associateWith { quadDigits.toSet() })
    }

    private fun checkEveryDigitQuad(unit: List<Int>) {
        val cells: List<Int> = quadCells(unit)
        for (first: Int in 1..6) {
            for (second: Int in (first + 1)..7) {
                for (third: Int in (second + 1)..8) {
                    for (fourth: Int in (third + 1)..9) {
                        val digits: List<Int> = listOf(first, second, third, fourth)
                        assertUnitEliminations(unit, cells, pattern(cells, digits), digits.toSet())
                    }
                }
            }
        }
    }

    private fun checkEveryCellQuad(unit: List<Int>) {
        for (first: Int in 0..5) {
            for (second: Int in (first + 1)..6) {
                for (third: Int in (second + 1)..7) {
                    for (fourth: Int in (third + 1)..8) {
                        val cells: List<Int> = listOf(unit[first], unit[second], unit[third], unit[fourth])
                        assertUnitEliminations(unit, cells, pattern(cells, quadDigits), quadDigits.toSet())
                    }
                }
            }
        }
    }

    private fun candidatePatterns(): List<List<Set<Int>>> {
        val patterns: MutableList<List<Set<Int>>> = mutableListOf()
        for (first: Int in 2..4) {
            for (second: Int in 2..4) {
                for (third: Int in 2..4) {
                    for (fourth: Int in 2..4) {
                        patterns.add(candidatePattern(listOf(first, second, third, fourth)))
                    }
                }
            }
        }
        return patterns
    }

    private fun candidatePattern(sizes: List<Int>): List<Set<Int>> =
        sizes.mapIndexed { cell: Int, size: Int ->
            List(size) { offset: Int -> quadDigits[(cell + offset) % quadDigits.size] }.toSet()
        }

    private fun pattern(cells: List<Int>, digits: List<Int>): LogicalState = stateWithCandidates(
        cells[0] to setOf(digits[0], digits[1]), cells[1] to setOf(digits[1], digits[2]),
        cells[2] to setOf(digits[2], digits[3]), cells[3] to setOf(digits[3], digits[0]),
    )

    private fun assertUnitEliminations(
        unit: List<Int>,
        cells: List<Int>,
        state: LogicalState,
        digits: Set<Int>,
    ) {
        val targets: List<Int> = unit - cells.toSet()
        assertions.assertEliminations(state, targets.associateWith { digits })
    }

    private fun quadCells(unit: List<Int>): List<Int> = listOf(unit.first(), unit[2], unit[4], unit.last())

    private fun stateWithCandidates(vararg changes: Pair<Int, Set<Int>>): LogicalState =
        LogicalStates.withCandidates(emptyState, *changes)

    private fun withoutQuadDigitsOutside(
        state: LogicalState,
        cells: List<Int>,
        digits: Set<Int>,
    ): LogicalState = state.copy(candidates = state.candidates.mapIndexed { cell: Int, candidates: Set<Int> ->
        if (cell in cells) candidates else candidates - digits
    })
}
