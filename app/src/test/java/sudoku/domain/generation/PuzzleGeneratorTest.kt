package sudoku.domain.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.generation.ClueRemovalResult.Removed
import sudoku.domain.solver.SolutionCount
import sudoku.fixtures.TestBoards

class PuzzleGeneratorTest {
    @Test
    fun passesTheGeneratedSolutionToTheRemoverAndReturnsThePlayableBoard() {
        val solution: SudokuBoard = TestBoards.solved()
        val generator: PuzzleGenerator = PuzzleGenerator({ solution }, ClueRemover(countSolutions = {
            assertTrue(it.cells.indices.all { cell: Int -> it[cell] == 0 || it[cell] == solution[cell] })
            SolutionCount.UNIQUE
        }))
        val result: Removed = generator.generate(80) as ClueRemovalResult.Removed
        assertTrue(result.targetReached)
        assertEquals(1, result.puzzle.cells.count { it == 0 })
    }

    @Test
    fun reportsTheMissingCounterInsteadOfReturningTheSolvedBoardAsAPlayablePuzzle() {
        val remover: ClueRemover = ClueRemover(countSolutions = { SolutionCount.NOT_IMPLEMENTED })
        val generator: PuzzleGenerator = PuzzleGenerator(TestBoards::solved, remover)
        assertEquals(ClueRemovalResult.CounterRequired, generator.generate(80))
    }

    @Test
    fun rejectsAFullyFilledPuzzleBeforeStartingGeneration() {
        val generator: PuzzleGenerator = PuzzleGenerator(generateSolution = { error("Generation must not start.") })
        assertThrows(IllegalArgumentException::class.java) { generator.generate(81) }
    }

    @Test
    fun retriesUnreachableTargetsAndStopsAsSoonAsTheTargetIsReached() {
        var checks: Int = 0
        var attempts: Int = 0
        val generator: PuzzleGenerator =
            PuzzleGenerator({ attempts++; TestBoards.solved() }, ClueRemover(countSolutions = {
                if (++checks <= 81) SolutionCount.MULTIPLE else SolutionCount.UNIQUE
            }))
        val result: Removed = generator.generate(80, 3) as ClueRemovalResult.Removed
        assertTrue(result.targetReached)
        assertEquals(2, attempts)
        assertEquals(82, checks)
    }

    @Test
    fun exhaustionReportsTheUnreachedTargetAndRespectsTheAttemptLimit() {
        var attempts: Int = 0
        val generator: PuzzleGenerator =
            PuzzleGenerator({ attempts++; TestBoards.solved() }, ClueRemover(countSolutions = {
                SolutionCount.MULTIPLE
            }))
        val result: Removed = generator.generate(80, 3) as ClueRemovalResult.Removed
        assertFalse(result.targetReached)
        assertEquals(3, attempts)
        assertThrows(IllegalArgumentException::class.java) { generator.generate(80, 0) }
    }

    @Test
    fun missingCountersDoNotTriggerFurtherAttempts() {
        var attempts: Int = 0
        val generator: PuzzleGenerator =
            PuzzleGenerator({ attempts++; TestBoards.solved() }, ClueRemover(countSolutions = {
                SolutionCount.NOT_IMPLEMENTED
            }))
        assertEquals(ClueRemovalResult.CounterRequired, generator.generate(80, 3))
        assertEquals(1, attempts)
    }
}
