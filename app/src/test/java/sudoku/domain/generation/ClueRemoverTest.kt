package sudoku.domain.generation

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.solver.SolutionCount
import sudoku.fixtures.TestBoards

class ClueRemoverTest {
    private val solution = TestBoards.solved()

    @Test
    fun keepsUniqueRemovalsUntilTheClueTargetIsReached() {
        var checks = 0
        val remover = ClueRemover(countSolutions = { checks++; SolutionCount.UNIQUE })
        val result = remover.removeClues(solution, 78) as ClueRemovalResult.Removed
        assertTrue(result.targetReached)
        assertEquals(3, checks)
        assertEquals(78, result.puzzle.cells.count { it != 0 })
        assertTrue(result.puzzle.cells.indices.all { result.puzzle[it] in listOf(0, solution[it]) })
    }

    @Test
    fun restoresAnAmbiguousRemovalBeforeTryingAnotherCell() {
        val candidates = mutableListOf<SudokuBoard>()
        val remover = ClueRemover(countSolutions = {
            candidates.add(it)
            if (candidates.size == 1) SolutionCount.MULTIPLE else SolutionCount.UNIQUE
        })
        val result = remover.removeClues(solution, 80) as ClueRemovalResult.Removed
        val rejectedCell = candidates.first().cells.indexOf(0)
        assertEquals(2, candidates.size)
        assertEquals(solution[rejectedCell], result.puzzle[rejectedCell])
        assertEquals(candidates.last().cells, result.puzzle.cells)
    }

    @Test
    fun reportsAnUnreachableTargetWithoutKeepingAmbiguousRemovals() {
        val remover = ClueRemover(countSolutions = { SolutionCount.MULTIPLE })
        val result = remover.removeClues(solution, 80) as ClueRemovalResult.Removed
        assertFalse(result.targetReached)
        assertEquals(solution.cells, result.puzzle.cells)
    }

    @Test
    fun anUnimplementedCounterDoesNotReturnAnUncheckedPuzzle() {
        val remover = ClueRemover(countSolutions = { SolutionCount.NOT_IMPLEMENTED })
        assertEquals(ClueRemovalResult.CounterRequired, remover.removeClues(solution, 80))
    }

    @Test
    fun anImpossibleCounterResultIsReportedAsAnError() {
        val remover = ClueRemover(countSolutions = { SolutionCount.NONE })
        assertThrows(IllegalStateException::class.java) { remover.removeClues(solution, 80) }
    }

    @Test
    fun freshSeededRemoversRepeatTheSameRemovalOrder() {
        val first = ClueRemover({ SolutionCount.UNIQUE }, Random(42)).removeClues(solution, 60)
        val second = ClueRemover({ SolutionCount.UNIQUE }, Random(42)).removeClues(solution, 60)
        assertEquals((first as ClueRemovalResult.Removed).puzzle.cells,
            (second as ClueRemovalResult.Removed).puzzle.cells)
    }

    @Test
    fun anAlreadyReachedTargetDoesNotCallTheCounter() {
        val remover = ClueRemover(countSolutions = { error("No removal should be attempted.") })
        val result = remover.removeClues(solution, 81) as ClueRemovalResult.Removed
        assertTrue(result.targetReached)
        assertEquals(solution.cells, result.puzzle.cells)
    }

    @Test
    fun rejectsIncompleteSolutionsAndInvalidTargets() {
        val remover = ClueRemover()
        assertThrows(IllegalArgumentException::class.java) { remover.removeClues(SudokuBoard.empty(), 30) }
        assertThrows(IllegalArgumentException::class.java) { remover.removeClues(solution, -1) }
        assertThrows(IllegalArgumentException::class.java) { remover.removeClues(solution, 82) }
    }
}
