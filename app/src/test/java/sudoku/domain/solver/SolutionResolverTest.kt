package sudoku.domain.solver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import sudoku.data.puzzle.SamplePuzzle
import sudoku.fixtures.TestBoards

class SolutionResolverTest {
    @Test
    fun resolvesTheReferenceSolutionWithoutChangingThePuzzle() {
        val puzzle = SamplePuzzle.create().puzzle
        val before = puzzle.cells.toList()
        assertEquals(TestBoards.solved().cells, SolutionResolver().resolve(puzzle)!!.cells)
        assertEquals(before, puzzle.cells)
    }

    @Test
    fun conflictingStartingNumbersHaveNoReferenceSolution() {
        assertNull(SolutionResolver().resolve(TestBoards.solved().withValue(0, 1)))
    }
}
