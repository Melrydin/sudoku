package sudoku.domain.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.domain.solver.SolutionCount
import sudoku.fixtures.TestBoards

class PuzzleGeneratorTest {
    @Test
    fun passesTheGeneratedSolutionToTheRemoverAndReturnsThePlayableBoard() {
        val solution = TestBoards.solved()
        val generator = PuzzleGenerator({ solution }, ClueRemover(countSolutions = {
            assertTrue(it.cells.indices.all { cell -> it[cell] == 0 || it[cell] == solution[cell] })
            SolutionCount.UNIQUE
        }))
        val result = generator.generate(80) as ClueRemovalResult.Removed
        assertTrue(result.targetReached)
        assertEquals(1, result.puzzle.cells.count { it == 0 })
    }

    @Test
    fun reportsTheMissingCounterInsteadOfReturningTheSolvedBoardAsAPlayablePuzzle() {
        val remover = ClueRemover(countSolutions = { SolutionCount.NOT_IMPLEMENTED })
        val generator = PuzzleGenerator(TestBoards::solved, remover)
        assertEquals(ClueRemovalResult.CounterRequired, generator.generate(80))
    }

    @Test
    fun rejectsAFullyFilledPuzzleBeforeStartingGeneration() {
        val generator = PuzzleGenerator(generateSolution = { error("Generation must not start.") })
        assertThrows(IllegalArgumentException::class.java) { generator.generate(81) }
    }
}
