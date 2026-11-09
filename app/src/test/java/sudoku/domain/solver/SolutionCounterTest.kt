package sudoku.domain.solver

import org.junit.Assert.assertEquals
import org.junit.Test
import sudoku.data.puzzle.SamplePuzzle
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuRules
import sudoku.fixtures.TestBoards

class SolutionCounterTest {
    private val counter = SolutionCounter()

    @Test
    fun aCompletedValidBoardHasExactlyOneSolution() {
        assertEquals(SolutionCount.UNIQUE, counter.count(TestBoards.solved()))
    }

    @Test
    fun theSamplePuzzleHasExactlyOneSolution() {
        assertEquals(SolutionCount.UNIQUE, counter.count(SamplePuzzle.create().puzzle))
    }

    @Test
    fun conflictingGivensHaveNoSolution() {
        assertEquals(SolutionCount.NONE, counter.count(TestBoards.solved().withValue(0, 3)))
    }

    @Test
    fun aConflictFreeBoardCanStillHaveNoSolution() {
        val board = TestBoards.solved().withValue(0, 1).withValue(7, 0).withValue(18, 0)
        assertEquals(emptySet<Int>(), SudokuRules.conflicts(board))
        assertEquals(SolutionCount.NONE, counter.count(board))
    }

    @Test(timeout = 2000)
    fun anEmptyBoardStopsAfterFindingTwoSolutions() {
        assertEquals(SolutionCount.MULTIPLE, counter.count(SudokuBoard.empty()))
    }

    @Test
    fun aDigitSwapAmbiguityIsReportedWithoutChangingTheInput() {
        val board = SudokuBoard.from(TestBoards.solved().cells.map { if (it in 1..2) 0 else it })
        val before = board.cells.toList()
        assertEquals(SolutionCount.MULTIPLE, counter.count(board))
        assertEquals(before, board.cells)
        assertEquals(SolutionCount.UNIQUE, counter.count(TestBoards.solved()))
    }
}
