package sudoku.domain.grading.techniques

import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.fixtures.LogicalStates
import sudoku.fixtures.TestBoards
import sudoku.testing.TechniqueAssertions

class HiddenTripleDetectorTest {
    private val assertions: TechniqueAssertions =
        TechniqueAssertions(HiddenTripleDetector(), SolvingTechnique.HIDDEN_TRIPLE)
    private val allDigits: Set<Int> = (1..9).toSet()
    private val emptyState: LogicalState = LogicalState.from(SudokuBoard.empty())

    @Test
    fun findsEveryDigitTripleInEveryRowColumnAndBox() {
        for (unit: List<Int> in SudokuUnits.all) {
            checkEveryDigitTriple(unit)
        }
    }

    @Test
    fun acceptsOverlappingTwoAndThreePositionPatternsInEveryUnit() {
        for (unit: List<Int> in SudokuUnits.all) {
            val cells: List<Int> = tripleCells(unit)
            val options: List<List<Int>> = cells.map { omitted: Int -> cells - omitted } + listOf(cells)
            checkPositionPatterns(unit, cells, options)
        }
    }

    @Test
    fun removesOnlyExtraCandidatesAndIgnoresCandidateOrder() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 4, 8), setOf(2, 7, 9))
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 0 to linkedSetOf(7, 1, 2), 4 to linkedSetOf(9, 2, 4), 8 to linkedSetOf(5, 9, 7),
        )
        assertions.assertEliminations(state, mapOf(0 to setOf(1), 4 to setOf(4), 8 to setOf(5)))
    }

    @Test
    fun omitsTripleCellsWithoutExtraCandidates() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 4, 8), setOf(2, 7, 9))
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(2, 7), 4 to setOf(2, 9), 8 to setOf(5, 7, 9),
        )
        assertions.assertEliminations(state, mapOf(8 to setOf(5)))
    }

    @Test
    fun returnsNotFoundOnAnEmptyOrCompletedBoard() {
        assertions.assertNotFound(emptyState)
        assertions.assertNotFound(LogicalState.from(TestBoards.solved()))
    }

    @Test
    fun rejectsATripleWhenAnyDigitHasAFourthPosition() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 4, 8), setOf(2, 7, 9))
        for (digit: Int in setOf(2, 7, 9)) {
            val state: LogicalState = LogicalStates.withCandidates(initial, 1 to (initial.candidates[1] + digit))
            assertions.assertNotFound(state)
        }
    }

    @Test
    fun rejectsThreeDigitsRestrictedToOnlyTwoCells() {
        assertions.assertNotFound(pattern(SudokuUnits.rows.first(), listOf(0, 8), setOf(2, 7, 9)))
    }

    @Test
    fun requiresEveryTripleDigitToHaveAPosition() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, listOf(0, 4, 8), setOf(2, 7, 9))
        for (digit: Int in setOf(2, 7, 9)) {
            val state: LogicalState = LogicalStates.restrictDigit(initial, unit, emptyList(), digit)
            assertions.assertNotFound(state)
        }
    }

    @Test
    fun doesNotCombinePositionsFromDifferentUnits() {
        val state: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 4), setOf(2, 7))
        val restricted: LogicalState = LogicalStates.restrictDigit(state, SudokuUnits.columns.last(), listOf(80), 9)
        assertions.assertNotFound(restricted)
    }

    @Test
    fun acceptsATripleContainingASingleCandidatePosition() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, listOf(0, 4, 8), setOf(2, 7, 9))
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(1, 2), 4 to setOf(4, 7, 9), 8 to setOf(5, 7, 9),
        )
        assertions.assertEliminations(state, mapOf(0 to setOf(1), 4 to setOf(4), 8 to setOf(5)))
    }

    @Test
    fun returnsNotFoundWithoutEffectiveEliminations() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 4, 8), setOf(2, 7, 9))
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(2, 7), 4 to setOf(2, 9), 8 to setOf(7, 9),
        )
        assertions.assertNotFound(state)
    }

    @Test
    fun continuesAfterAnIneffectiveCombinationInTheSameUnit() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, listOf(0, 4, 8), setOf(1, 2, 3))
        val ineffective: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(1, 2), 4 to setOf(1, 3), 8 to setOf(2, 3),
        )
        val state: LogicalState = restrictDigits(ineffective, unit, listOf(1, 3, 5), setOf(4, 5, 6))
        assertions.assertEliminations(state, listOf(1, 3, 5).associateWith { setOf(7, 8, 9) })
    }

    @Test
    fun continuesAfterAnIneffectiveUnit() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 4, 8), setOf(1, 2, 3))
        val ineffective: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(1, 2), 4 to setOf(1, 3), 8 to setOf(2, 3),
        )
        val state: LogicalState = restrictDigits(ineffective, SudokuUnits.rows[3], listOf(27, 31, 35), setOf(4, 5, 6))
        assertions.assertEliminations(state, listOf(27, 31, 35).associateWith { allDigits - setOf(4, 5, 6) })
    }

    @Test
    fun searchesPastInvalidCombinationsAndFindsTheLastDigitTriple() {
        val state: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 4, 8), setOf(7, 8, 9))
        assertions.assertEliminations(state, listOf(0, 4, 8).associateWith { setOf(1, 2, 3, 4, 5, 6) })
    }

    @Test
    fun returnsOnlyTheFirstEffectiveTriple() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, listOf(0, 4, 8), setOf(1, 2, 3))
        val state: LogicalState = restrictDigits(initial, unit, listOf(1, 3, 5), setOf(4, 5, 6))
        assertions.assertEliminations(state, listOf(0, 4, 8).associateWith { setOf(7, 8, 9) })
    }

    @Test
    fun searchesRowsBeforeColumns() {
        val initial: LogicalState = pattern(SudokuUnits.rows.last(), listOf(72, 76, 80), setOf(2, 7, 9))
        val state: LogicalState = restrictDigits(initial, SudokuUnits.columns[1], listOf(1, 28, 55), setOf(1, 3, 5))
        assertions.assertEliminations(state, listOf(72, 76, 80).associateWith { allDigits - setOf(2, 7, 9) })
    }

    @Test
    fun searchesColumnsBeforeBoxes() {
        val initial: LogicalState = pattern(SudokuUnits.columns.last(), listOf(8, 44, 80), setOf(2, 7, 9))
        val state: LogicalState = restrictDigits(initial, SudokuUnits.boxes.first(), listOf(0, 10, 20), setOf(1, 3, 5))
        assertions.assertEliminations(state, listOf(8, 44, 80).associateWith { allDigits - setOf(2, 7, 9) })
    }

    @Test
    fun neverEliminatesFromFilledCells() {
        val initial: LogicalState = LogicalState.from(SudokuBoard.empty().withValue(1, 1))
        val state: LogicalState = restrictDigits(initial, SudokuUnits.rows.first(), listOf(0, 4, 8), setOf(2, 7, 9))
        assertions.assertEliminations(state, listOf(0, 4, 8).associateWith { allDigits - setOf(1, 2, 7, 9) })
    }

    private fun checkEveryDigitTriple(unit: List<Int>) {
        val cells: List<Int> = tripleCells(unit)
        for (first: Int in 1..7) {
            for (second: Int in (first + 1)..8) {
                for (third: Int in (second + 1)..9) {
                    val digits: Set<Int> = setOf(first, second, third)
                    val expected: Map<Int, Set<Int>> = cells.associateWith { allDigits - digits }
                    assertions.assertEliminations(pattern(unit, cells, digits), expected)
                }
            }
        }
    }

    private fun checkPositionPatterns(unit: List<Int>, cells: List<Int>, options: List<List<Int>>) {
        for (first: List<Int> in options) {
            for (second: List<Int> in options) {
                for (third: List<Int> in options) {
                    if ((first + second + third).toSet().size != 3) continue
                    val initial: LogicalState = LogicalStates.restrictDigit(emptyState, unit, first, 2)
                    val middle: LogicalState = LogicalStates.restrictDigit(initial, unit, second, 7)
                    val state: LogicalState = LogicalStates.restrictDigit(middle, unit, third, 9)
                    assertions.assertEliminations(state, cells.associateWith { allDigits - setOf(2, 7, 9) })
                }
            }
        }
    }

    private fun tripleCells(unit: List<Int>): List<Int> = listOf(unit.first(), unit[4], unit.last())

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
}
