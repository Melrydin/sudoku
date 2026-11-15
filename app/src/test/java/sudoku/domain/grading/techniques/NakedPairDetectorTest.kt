package sudoku.domain.grading.techniques

import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.fixtures.LogicalStates
import sudoku.fixtures.TestBoards
import sudoku.testing.TechniqueAssertions

class NakedPairDetectorTest {
    private val assertions = TechniqueAssertions(NakedPairDetector(), SolvingTechnique.NAKED_PAIR)

    @Test
    fun findsEveryDigitPairInEveryRowColumnAndBox() {
        for (unit in SudokuUnits.all) {
            for (first in 1..8) {
                for (second in first + 1..9) {
                    val digits = setOf(first, second)
                    val pair = listOf(unit.first(), unit.last())
                    val state = stateWithCandidates(*pair.map { it to digits }.toTypedArray())
                    assertions.assertEliminations(
                        state,
                        (unit - pair.toSet()).associateWith { digits })
                }
            }
        }
    }

    @Test
    fun removesOnlyPresentPairDigitsAndIgnoresCandidateOrder() {
        val state = stateWithCandidates(
            0 to setOf(2, 7), 4 to linkedSetOf(7, 2),
            1 to setOf(2, 5, 7), 2 to setOf(1, 7, 9), 3 to setOf(3, 8, 9),
            5 to setOf(2, 6, 8), 6 to setOf(1), 7 to setOf(3, 4, 5), 8 to setOf(6, 8, 9),
        )
        assertions.assertEliminations(state, mapOf(1 to setOf(2, 7), 2 to setOf(7), 5 to setOf(2)))
    }

    @Test
    fun returnsNotFoundOnAnEmptyOrCompletedBoard() {
        assertions.assertNotFound(LogicalState.from(SudokuBoard.empty()))
        assertions.assertNotFound(LogicalState.from(TestBoards.solved()))
    }

    @Test
    fun requiresExactlyTwoCandidatesInBothCells() {
        assertions.assertNotFound(stateWithCandidates(0 to setOf(2, 7), 4 to setOf(2, 7, 9)))
        assertions.assertNotFound(stateWithCandidates(0 to setOf(2), 4 to setOf(2)))
        assertions.assertNotFound(stateWithCandidates(0 to setOf(2, 7, 9), 4 to setOf(2, 7, 9)))
    }

    @Test
    fun rejectsDifferentPairsAndCellsWithoutASharedUnit() {
        assertions.assertNotFound(stateWithCandidates(0 to setOf(2, 7), 4 to setOf(2, 8)))
        assertions.assertNotFound(stateWithCandidates(0 to setOf(2, 7), 40 to setOf(2, 7)))
    }

    @Test
    fun rejectsThreeCellsWithTheSameTwoCandidates() {
        val state = stateWithCandidates(0 to setOf(2, 7), 4 to setOf(2, 7), 8 to setOf(2, 7))
        assertions.assertNotFound(state)
    }

    @Test
    fun returnsNotFoundWhenThePairHasNoEffectiveEliminations() {
        val initial = stateWithCandidates(0 to setOf(1, 2), 4 to setOf(1, 2))
        assertions.assertNotFound(withoutPairDigitsOutside(initial, listOf(0, 4)))
    }

    @Test
    fun continuesAfterAnIneffectivePairInTheSameUnit() {
        val initial = stateWithCandidates(0 to setOf(1, 2), 4 to setOf(1, 2))
        val ineffective = withoutPairDigitsOutside(initial, listOf(0, 4))
        val state = LogicalStates.withCandidates(ineffective, 1 to setOf(3, 4), 5 to setOf(3, 4))
        assertions.assertEliminations(state, listOf(2, 3, 6, 7, 8).associateWith { setOf(3, 4) })
    }

    @Test
    fun continuesAfterAnIneffectiveUnit() {
        val initial = stateWithCandidates(0 to setOf(1, 2), 4 to setOf(1, 2))
        val ineffective = withoutPairDigitsOutside(initial, listOf(0, 4))
        val state = LogicalStates.withCandidates(ineffective, 27 to setOf(3, 4), 31 to setOf(3, 4))
        val targets = SudokuUnits.rows[3] - setOf(27, 31)
        assertions.assertEliminations(state, targets.associateWith { setOf(3, 4) })
    }

    @Test
    fun returnsOnlyTheFirstEffectivePair() {
        val state = stateWithCandidates(
            0 to setOf(2, 7), 4 to setOf(2, 7), 1 to setOf(3, 8), 5 to setOf(3, 8),
        )
        assertions.assertEliminations(state, listOf(2, 3, 6, 7, 8).associateWith { setOf(2, 7) })
    }

    @Test
    fun searchesRowsBeforeColumns() {
        val state = stateWithCandidates(
            72 to setOf(2, 7), 76 to setOf(2, 7), 0 to setOf(3, 8), 36 to setOf(3, 8),
        )
        val targets = SudokuUnits.rows.last() - setOf(72, 76)
        assertions.assertEliminations(state, targets.associateWith { setOf(2, 7) })
    }

    @Test
    fun searchesColumnsBeforeBoxes() {
        val state = stateWithCandidates(
            8 to setOf(2, 7), 44 to setOf(2, 7), 0 to setOf(3, 8), 20 to setOf(3, 8),
        )
        val targets = SudokuUnits.columns.last() - setOf(8, 44)
        assertions.assertEliminations(state, targets.associateWith { setOf(2, 7) })
    }

    @Test
    fun neverEliminatesFromFilledCells() {
        val initial = LogicalState.from(SudokuBoard.empty().withValue(1, 9))
        val state = LogicalStates.withCandidates(initial, 0 to setOf(2, 7), 4 to setOf(2, 7))
        val targets = SudokuUnits.rows.first() - setOf(0, 1, 4)
        assertions.assertEliminations(state, targets.associateWith { setOf(2, 7) })
    }

    private fun stateWithCandidates(vararg changes: Pair<Int, Set<Int>>): LogicalState =
        LogicalStates.withCandidates(LogicalState.from(SudokuBoard.empty()), *changes)

    private fun withoutPairDigitsOutside(state: LogicalState, pair: List<Int>): LogicalState {
        val digits = state.candidates[pair.first()]
        return state.copy(candidates = state.candidates.mapIndexed { cell, candidates ->
            if (cell in pair) candidates else candidates - digits
        })
    }
}
