package sudoku.domain.grading.techniques

import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.fixtures.LogicalStates
import sudoku.fixtures.TestBoards
import sudoku.testing.TechniqueAssertions

class HiddenPairDetectorTest {
    private val assertions: TechniqueAssertions =
        TechniqueAssertions(HiddenPairDetector(), SolvingTechnique.HIDDEN_PAIR)
    private val allDigits: Set<Int> = (1..9).toSet()

    @Test
    fun findsEveryDigitPairInEveryRowColumnAndBox() {
        for (unit: List<Int> in SudokuUnits.all) {
            for (first: Int in 1..8) {
                for (second: Int in (first + 1)..9) {
                    val digits: Set<Int> = setOf(first, second)
                    val pair: List<Int> = listOf(unit.first(), unit.last())
                    val state: LogicalState = pattern(unit, pair, digits)
                    assertions.assertEliminations(state, pair.associateWith { allDigits - digits })
                }
            }
        }
    }

    @Test
    fun removesOnlyExtraCandidatesFromPairCellsAndIgnoresCandidateOrder() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 4), setOf(2, 7))
        val state: LogicalState = LogicalStates.withCandidates(
            initial, 0 to setOf(2, 4, 7), 4 to linkedSetOf(9, 7, 1, 2),
        )
        assertions.assertEliminations(state, mapOf(0 to setOf(4), 4 to setOf(1, 9)))
    }

    @Test
    fun findsEliminationsWhenOnlyOnePairCellHasExtraCandidates() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 4), setOf(2, 7))
        val state: LogicalState = LogicalStates.withCandidates(initial, 0 to setOf(2, 7), 4 to setOf(2, 7, 9))
        assertions.assertEliminations(state, mapOf(4 to setOf(9)))
    }

    @Test
    fun returnsNotFoundOnAnEmptyOrCompletedBoard() {
        assertions.assertNotFound(LogicalState.from(SudokuBoard.empty()))
        assertions.assertNotFound(LogicalState.from(TestBoards.solved()))
    }

    @Test
    fun rejectsDigitsWithDifferentCandidatePositions() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, listOf(0, 4), setOf(2))
        val state: LogicalState = LogicalStates.restrictDigit(initial, unit, listOf(0, 8), 7)
        assertions.assertNotFound(state)
    }

    @Test
    fun rejectsAPairWhenEitherDigitHasAThirdPosition() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 4), setOf(2, 7))
        for (digit: Int in setOf(2, 7)) {
            val state: LogicalState = LogicalStates.withCandidates(initial, 8 to (initial.candidates[8] + digit))
            assertions.assertNotFound(state)
        }
    }

    @Test
    fun leavesSingleCandidatePositionsToOtherTechniques() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val singleton: LogicalState = pattern(unit, listOf(0), setOf(2))
        val state: LogicalState = LogicalStates.restrictDigit(singleton, unit, listOf(0, 4), 7)
        assertions.assertNotFound(state)
        assertions.assertNotFound(pattern(unit, listOf(0), setOf(2, 7)))
    }

    @Test
    fun returnsNotFoundWhenNeitherPairCellHasExtraCandidates() {
        val digits: Set<Int> = setOf(2, 7)
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 4), digits)
        val state: LogicalState = LogicalStates.withCandidates(initial, 0 to digits, 4 to digits)
        assertions.assertNotFound(state)
    }

    @Test
    fun continuesAfterADigitWithoutAMatchingSecondDigit() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val unmatched: LogicalState = pattern(unit, listOf(0, 4), setOf(1))
        val state: LogicalState = restrictDigits(unmatched, unit, listOf(2, 6), setOf(3, 4))
        val expected: Set<Int> = allDigits - setOf(1, 3, 4)
        assertions.assertEliminations(state, mapOf(2 to expected, 6 to expected))
    }

    @Test
    fun continuesAfterAnIneffectivePairInTheSameUnit() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, listOf(0, 4), setOf(1, 2))
        val ineffective: LogicalState = LogicalStates.withCandidates(initial, 0 to setOf(1, 2), 4 to setOf(1, 2))
        val state: LogicalState = restrictDigits(ineffective, unit, listOf(2, 6), setOf(3, 4))
        val expected: Set<Int> = allDigits - setOf(1, 2, 3, 4)
        assertions.assertEliminations(state, mapOf(2 to expected, 6 to expected))
    }

    @Test
    fun continuesAfterAnIneffectiveUnit() {
        val initial: LogicalState = pattern(SudokuUnits.rows.first(), listOf(0, 4), setOf(1, 2))
        val ineffective: LogicalState = LogicalStates.withCandidates(initial, 0 to setOf(1, 2), 4 to setOf(1, 2))
        val state: LogicalState = restrictDigits(ineffective, SudokuUnits.rows[3], listOf(27, 31), setOf(3, 4))
        val expected: Set<Int> = allDigits - setOf(3, 4)
        assertions.assertEliminations(state, mapOf(27 to expected, 31 to expected))
    }

    @Test
    fun returnsOnlyTheFirstEffectivePair() {
        val unit: List<Int> = SudokuUnits.rows.first()
        val initial: LogicalState = pattern(unit, listOf(0, 4), setOf(2, 7))
        val state: LogicalState = restrictDigits(initial, unit, listOf(1, 5), setOf(3, 8))
        val expected: Set<Int> = allDigits - setOf(2, 3, 7, 8)
        assertions.assertEliminations(state, mapOf(0 to expected, 4 to expected))
    }

    @Test
    fun searchesRowsBeforeColumns() {
        val initial: LogicalState = pattern(SudokuUnits.rows.last(), listOf(72, 76), setOf(2, 7))
        val state: LogicalState = restrictDigits(initial, SudokuUnits.columns.first(), listOf(0, 36), setOf(3, 8))
        val expected: Map<Int, Set<Int>> = mapOf(72 to (allDigits - setOf(2, 3, 7, 8)), 76 to (allDigits - setOf(2, 7)))
        assertions.assertEliminations(state, expected)
    }

    @Test
    fun searchesColumnsBeforeBoxes() {
        val initial: LogicalState = pattern(SudokuUnits.columns.last(), listOf(8, 44), setOf(2, 7))
        val state: LogicalState = restrictDigits(initial, SudokuUnits.boxes.first(), listOf(0, 20), setOf(3, 8))
        val expected: Set<Int> = allDigits - setOf(2, 7)
        assertions.assertEliminations(state, mapOf(8 to expected, 44 to expected))
    }

    @Test
    fun neverEliminatesFromFilledCells() {
        val initial: LogicalState = LogicalState.from(SudokuBoard.empty().withValue(1, 9))
        val state: LogicalState = restrictDigits(initial, SudokuUnits.rows.first(), listOf(0, 4), setOf(2, 7))
        val expected: Set<Int> = allDigits - setOf(2, 7, 9)
        assertions.assertEliminations(state, mapOf(0 to expected, 4 to expected))
    }

    private fun pattern(unit: List<Int>, pair: List<Int>, digits: Set<Int>): LogicalState =
        restrictDigits(LogicalState.from(SudokuBoard.empty()), unit, pair, digits)

    private fun restrictDigits(
        state: LogicalState,
        unit: List<Int>,
        matchingCells: List<Int>,
        digits: Set<Int>,
    ): LogicalState = digits.fold(state) { restricted: LogicalState, digit: Int ->
        LogicalStates.restrictDigit(restricted, unit, matchingCells, digit)
    }
}
