package sudoku.domain.grading.techniques

import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.fixtures.LogicalStates
import sudoku.fixtures.TestBoards
import sudoku.testing.TechniqueAssertions

class HiddenQuadDetectorTest {
    private val assertions: TechniqueAssertions =
        TechniqueAssertions(HiddenQuadDetector(), SolvingTechnique.HIDDEN_QUAD)
    private val allDigits: Set<Int> = (1..9).toSet()
    private val quadDigits: List<Int> = listOf(2, 4, 7, 9)
    private val emptyState: LogicalState = LogicalState.from(SudokuBoard.empty())

    @Test
    fun findsEveryDigitQuadInEveryRowColumnAndBox() {
        SudokuUnits.all.forEach(::checkEveryDigitQuad)
    }

    @Test
    fun acceptsOverlappingTwoThreeAndFourPositionPatternsInEveryUnit() {
        val counts: List<List<Int>> = positionCounts()
        for (unit: List<Int> in SudokuUnits.all) {
            for (sizes: List<Int> in counts) {
                val state: LogicalState = positionPattern(unit, sizes)
                assertEliminations(state, quadCells(unit), allDigits - quadDigits.toSet())
            }
        }
    }

    @Test
    fun removesOnlyExtraCandidatesAndIgnoresCandidateOrder() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 2, 4, 8), quadDigits.toSet())
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 0 to linkedSetOf(4, 1, 2), 2 to linkedSetOf(7, 4, 3),
            4 to linkedSetOf(9, 5, 7), 8 to linkedSetOf(6, 9, 2),
        )
        assertions.assertEliminations(state, mapOf(0 to setOf(1), 2 to setOf(3), 4 to setOf(5), 8 to setOf(6)))
    }

    @Test
    fun omitsQuadCellsWithoutExtraCandidates() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 2, 4, 8), quadDigits.toSet())
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(2, 4), 2 to setOf(4, 7), 4 to setOf(7, 9), 8 to setOf(2, 6, 9),
        )
        assertions.assertEliminations(state, mapOf(8 to setOf(6)))
    }

    @Test
    fun returnsNotFoundOnAnEmptyOrCompletedBoard() {
        assertions.assertNotFound(emptyState)
        assertions.assertNotFound(LogicalState.from(TestBoards.solved()))
    }

    @Test
    fun rejectsAQuadWhenAnyDigitHasAFifthPosition() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 2, 4, 8), quadDigits.toSet())
        for (digit: Int in quadDigits) {
            val state: LogicalState = LogicalStates.withCandidates(initial, 1 to (initial.candidates[1] + digit))
            assertions.assertNotFound(state)
        }
    }

    @Test
    fun requiresExactlyFourDistinctCells() {
        for (count: Int in 1..3) {
            val cells: List<Int> = quadCells(SudokuUnits.rows.first()).take(count)
            assertions.assertNotFound(pattern(SudokuUnits.rows.first(), cells, quadDigits.toSet()))
        }
    }

    @Test
    fun requiresEveryQuadDigitToHaveAPosition() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, quadCells(unit), quadDigits.toSet())
        for (digit: Int in quadDigits) {
            val state: LogicalState = LogicalStates.restrictDigit(initial, unit, emptyList(), digit)
            assertions.assertNotFound(state)
        }
    }

    @Test
    fun doesNotCombinePositionsFromDifferentUnits() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 2, 4), setOf(2, 4, 7))
        val state: LogicalState = LogicalStates.restrictDigit(initial, SudokuUnits.columns.last(), listOf(80), 9)
        assertions.assertNotFound(state)
    }

    @Test
    fun acceptsAQuadContainingASingleCandidatePosition() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 2, 4, 8), quadDigits.toSet())
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(1, 2), 2 to setOf(3, 4, 7), 4 to setOf(5, 7, 9), 8 to setOf(4, 6, 9),
        )
        assertions.assertEliminations(state, mapOf(0 to setOf(1), 2 to setOf(3), 4 to setOf(5), 8 to setOf(6)))
    }

    @Test
    fun returnsNotFoundWithoutEffectiveEliminations() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 2, 4, 8), quadDigits.toSet())
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(2, 4), 2 to setOf(4, 7), 4 to setOf(7, 9), 8 to setOf(2, 9),
        )
        assertions.assertNotFound(state)
    }

    @Test
    fun continuesAfterAnIneffectiveCombinationInTheSameUnit() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, quadCells(unit), setOf(1, 2, 3, 4))
        val ineffective: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(1, 2), 2 to setOf(2, 3), 4 to setOf(3, 4), 8 to setOf(1, 4),
        )
        val state: LogicalState = restrictDigits(ineffective, unit, listOf(1, 3, 5, 7), setOf(5, 6, 7, 8))
        assertEliminations(state, listOf(1, 3, 5, 7), setOf(9))
    }

    @Test
    fun continuesAfterAnIneffectiveUnit() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, quadCells(unit), setOf(1, 2, 3, 4))
        val ineffective: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(1, 2), 2 to setOf(2, 3), 4 to setOf(3, 4), 8 to setOf(1, 4),
        )
        val cells: List<Int> = quadCells(SudokuUnits.rows[3])
        val state: LogicalState = restrictDigits(ineffective, SudokuUnits.rows[3], cells, setOf(5, 6, 7, 8))
        assertEliminations(state, cells, setOf(1, 2, 3, 4, 9))
    }

    @Test
    fun searchesPastInvalidCombinationsAndFindsTheLastDigitQuad() {
        val cells: List<Int> = quadCells(SudokuUnits.rows.first())
        val state: LogicalState = pattern(SudokuUnits.rows.first(), cells, setOf(6, 7, 8, 9))
        assertEliminations(state, cells, setOf(1, 2, 3, 4, 5))
    }

    @Test
    fun returnsOnlyTheFirstEffectiveQuad() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, quadCells(unit), setOf(1, 2, 3, 4))
        val state: LogicalState = restrictDigits(initial, unit, listOf(1, 3, 5, 7), setOf(5, 6, 7, 8))
        assertEliminations(state, quadCells(unit), setOf(9))
    }

    @Test
    fun searchesRowsBeforeColumns() {
        val unit: List<Int> = SudokuUnits.rows.last()
        val initial: LogicalState = pattern(unit, quadCells(unit), quadDigits.toSet())
        val state: LogicalState = restrictDigits(
            initial, SudokuUnits.columns[1], listOf(1, 19, 37, 55), setOf(1, 3, 5, 6),
        )
        assertEliminations(state, quadCells(unit), allDigits - quadDigits.toSet())
    }

    @Test
    fun searchesColumnsBeforeBoxes() {
        val unit: List<Int> = SudokuUnits.columns.last()
        val initial: LogicalState = pattern(unit, quadCells(unit), quadDigits.toSet())
        val state: LogicalState = restrictDigits(
            initial, SudokuUnits.boxes.first(), listOf(0, 2, 10, 20), setOf(1, 3, 5, 6),
        )
        assertEliminations(state, quadCells(unit), allDigits - quadDigits.toSet())
    }

    @Test
    fun neverEliminatesFromFilledCells() {
        val initial: LogicalState = LogicalState.from(SudokuBoard.empty().withValue(1, 1))
        val cells: List<Int> = quadCells(SudokuUnits.rows.first())
        val state: LogicalState = restrictDigits(initial, SudokuUnits.rows.first(), cells, quadDigits.toSet())
        assertEliminations(state, cells, setOf(3, 5, 6, 8))
    }

    private fun checkEveryDigitQuad(unit: List<Int>) {
        val cells: List<Int> = quadCells(unit)
        for (first: Int in 1..6) {
            for (second: Int in (first + 1)..7) {
                for (third: Int in (second + 1)..8) {
                    for (fourth: Int in (third + 1)..9) {
                        val digits: Set<Int> = setOf(first, second, third, fourth)
                        assertEliminations(pattern(unit, cells, digits), cells, allDigits - digits)
                    }
                }
            }
        }
    }

    private fun positionCounts(): List<List<Int>> {
        val counts: MutableList<List<Int>> = mutableListOf()
        for (first: Int in 2..4) {
            for (second: Int in 2..4) {
                for (third: Int in 2..4) {
                    for (fourth: Int in 2..4) {
                        counts.add(listOf(first, second, third, fourth))
                    }
                }
            }
        }
        return counts
    }

    private fun positionPattern(unit: List<Int>, sizes: List<Int>): LogicalState {
        val cells: List<Int> = quadCells(unit)
        var state: LogicalState = emptyState
        for (index: Int in sizes.indices) {
            val positions: List<Int> = List(sizes[index]) { offset: Int -> cells[(index + offset) % cells.size] }
            state = LogicalStates.restrictDigit(state, unit, positions, quadDigits[index])
        }
        return state
    }

    private fun quadCells(unit: List<Int>): List<Int> = listOf(unit.first(), unit[2], unit[4], unit.last())

    private fun pattern(unit: List<Int>, cells: List<Int>, digits: Set<Int>): LogicalState =
        restrictDigits(emptyState, unit, cells, digits)

    private fun restrictDigits(
        state: LogicalState,
        unit: List<Int>,
        cells: List<Int>,
        digits: Set<Int>,
    ): LogicalState = digits.fold(state) { restricted: LogicalState, digit: Int ->
        LogicalStates.restrictDigit(restricted, unit, cells, digit)
    }

    private fun assertEliminations(state: LogicalState, cells: List<Int>, digits: Set<Int>) {
        assertions.assertEliminations(state, cells.associateWith { digits })
    }
}
