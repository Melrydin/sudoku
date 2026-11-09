package sudoku.game

import org.junit.Assert.assertEquals
import org.junit.Test

class DigitCountsTest {
    @Test
    fun countsGivensAndPlayerEntriesAndUpdatesAfterErasing() {
        val board = SamplePuzzle.create().board
        assertEquals(6, DigitCounts.remaining(board, 5))
        val edited = board.withValue(2, 5)
        assertEquals(5, DigitCounts.remaining(edited, 5))
        assertEquals(6, DigitCounts.remaining(edited.withValue(2, 0), 5))
    }

    @Test
    fun emptyBoardHasNineRemainingAndSolvedBoardHasNone() {
        for (digit in 1..9) {
            assertEquals(9, DigitCounts.remaining(TestBoards.empty(), digit))
            assertEquals(0, DigitCounts.remaining(TestBoards.solved(), digit))
        }
    }

    @Test
    fun conflictingOverfilledDigitsNeverHaveNegativeCounts() {
        val board = SudokuBoard.from(List(SudokuBoard.CELL_COUNT) { 1 })
        assertEquals(0, DigitCounts.remaining(board, 1))
    }
}
