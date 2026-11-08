package com.example.sudoku.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuRulesTest {
    @Test
    fun identifiesRowColumnAndBoxConflicts() {
        for (peer in listOf(8, 72, 10)) {
            val board = TestBoards.empty().withValue(0, 7).withValue(peer, 7)
            assertEquals(setOf(0, peer), SudokuRules.conflicts(board))
        }
    }

    @Test
    fun allowsEqualNumbersOutsidePeerGroups() {
        val board = TestBoards.empty().withValue(0, 7).withValue(40, 7)
        assertTrue(SudokuRules.conflicts(board).isEmpty())
    }

    @Test
    fun completionRequiresEveryCellAndValidGroups() {
        assertTrue(SudokuRules.isComplete(TestBoards.solved()))
        assertFalse(SudokuRules.isComplete(SamplePuzzle.create().board))
        assertFalse(SudokuRules.isComplete(TestBoards.solved().withValue(0, 3)))
    }

    @Test
    fun samplePuzzleContainsOnlyConsistentStartingNumbers() {
        assertTrue(SudokuRules.conflicts(SamplePuzzle.create().puzzle).isEmpty())
    }

    @Test
    fun boardsCopyTheSourceCells() {
        val source = MutableList(SudokuBoard.CELL_COUNT) { 0 }
        val board = SudokuBoard.from(source)
        source[0] = 9
        assertEquals(0, board[0])
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMalformedBoards() {
        SudokuBoard.parse("123")
    }
}
