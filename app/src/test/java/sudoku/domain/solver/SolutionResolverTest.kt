package sudoku.domain.solver

import org.junit.Assert.assertEquals
import org.junit.Test
import sudoku.data.puzzle.SamplePuzzle
import sudoku.domain.board.SudokuBoard
import sudoku.domain.common.OptionalValue
import sudoku.fixtures.TestBoards

class SolutionResolverTest {
    @Test
    fun resolvesTheReferenceSolutionWithoutChangingThePuzzle() {
        val puzzle: SudokuBoard = SamplePuzzle.create().puzzle
        val before: List<Int> = puzzle.cells.toList()
        assertEquals(TestBoards.solved().cells, SolutionResolver().resolve(puzzle).get().cells)
        assertEquals(before, puzzle.cells)
    }

    @Test
    fun conflictingStartingNumbersHaveNoReferenceSolution() {
        assertEquals(OptionalValue.Absent, SolutionResolver().resolve(TestBoards.solved().withValue(0, 1)))
    }
}
