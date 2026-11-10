package sudoku.domain.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.data.puzzle.SamplePuzzle
import sudoku.domain.generation.ClueRemovalResult
import sudoku.domain.generation.ClueRemover
import sudoku.domain.generation.GeneratorConfig
import sudoku.domain.generation.PuzzleRandom
import sudoku.domain.generation.SudokuGenerator
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.GeneratorVersion
import sudoku.domain.puzzle.PuzzleId
import sudoku.domain.solver.SolutionCount
import sudoku.domain.solver.SolutionCounter

class NewGameFactoryTest {
    private val puzzle = PuzzleId(Difficulty.EASY, 42, GeneratorConfig.VERSION)

    @Test(timeout = 5000)
    fun generatedGamesArePlayableUniqueAndStartWithoutPlayerEntries() {
        val state = NewGameFactory.create(puzzle, false).state
        assertFalse(state.isComplete)
        assertFalse(state.hasEntries)
        assertTrue(state.puzzle.cells.count { it != 0 } in GeneratorConfig.clueRange(puzzle.difficulty))
        assertEquals(SolutionCount.UNIQUE, SolutionCounter().count(state.puzzle))
        assertEquals(state.puzzle.cells, state.board.cells)
    }

    @Test(timeout = 5000)
    fun theSameLevelIsIndependentOfPreviouslyGeneratedLevels() {
        val first = NewGameFactory.create(puzzle, false).state.puzzle
        val other = NewGameFactory.create(puzzle.copy(level = 43), false).state.puzzle
        val repeated = NewGameFactory.create(puzzle, false).state.puzzle
        assertNotEquals(first.cells, other.cells)
        assertEquals(first.cells, repeated.cells)
    }

    @Test(timeout = 5000)
    fun difficultyIsPartOfThePuzzleIdentity() {
        val first = NewGameFactory.create(puzzle, false).state.puzzle
        val other = NewGameFactory.create(puzzle.copy(difficulty = Difficulty.MEDIUM), false).state.puzzle
        assertNotEquals(first.cells, other.cells)
    }

    @Test(timeout = 5000)
    fun generatedGamesRespectThePrefillOption() {
        val state = NewGameFactory.create(puzzle, true).state
        assertEquals(GameNotes.prefill(state.puzzle), state.notes)
        assertTrue(state.puzzle.cells.indices.filter { state.puzzle[it] != 0 }.all { state.notes[it].isEmpty() })
    }

    @Test(timeout = 30000)
    fun allDifficultiesGenerateUniquePuzzlesWithVaryingClueCountsInTheirRange() {
        Difficulty.entries.forEach { difficulty ->
            val counts = (1..10).map { level ->
                val id = puzzle.copy(difficulty = difficulty, level = level)
                val state = NewGameFactory.create(id, false).state
                assertEquals(expectedTarget(id), state.puzzle.cells.count { it != 0 })
                assertEquals(SolutionCount.UNIQUE, SolutionCounter().count(state.puzzle))
                state.puzzle.cells.count { it != 0 }
            }
            val range = GeneratorConfig.clueRange(difficulty)
            assertTrue("Clue counts for $difficulty: $counts", counts.all { it in range })
            assertTrue("Clue counts must vary for $difficulty", counts.distinct().size > 1)
        }
    }

    @Test(timeout = 5000)
    fun versionOneKeepsItsOriginalRandomSequenceAndFixedTarget() {
        val legacy = puzzle.copy(version = GeneratorVersion.V1)
        val random = PuzzleRandom.forPuzzle(legacy)
        val solution = SudokuGenerator(random).generate()
        val expected = ClueRemover(random = random).removeClues(solution, 36) as ClueRemovalResult.Removed
        val session = NewGameFactory.create(legacy, false)
        assertEquals(expected.puzzle.cells, session.state.puzzle.cells)
        assertEquals(solution.cells, session.solution.cells)
    }

    private fun expectedTarget(puzzle: PuzzleId): Int {
        val random = PuzzleRandom.forPuzzle(puzzle)
        SudokuGenerator(random).generate()
        return GeneratorConfig.targetClues(puzzle, random)
    }

    @Test
    fun restartingKeepsThePuzzleAndClearsEntriesHistoryAndTime() {
        val session = NewGameFactory.fromPuzzle(SamplePuzzle.create().puzzle, false)
        session.select(2)
        session.enter(4)
        session.trackElapsedTime(1000)
        val restarted = NewGameFactory.fromPuzzle(session.state.puzzle, false)
        assertFalse(restarted.state.hasEntries)
        assertFalse(restarted.canUndo)
        assertEquals(0L, restarted.elapsedMillis)
        assertEquals(session.state.puzzle.cells, restarted.state.puzzle.cells)
    }
}
