package sudoku.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.generator.GeneratorConfig
import sudoku.generator.SolutionCount
import sudoku.generator.SolutionCounter

class NewGameFactoryTest {
    private val puzzle = PuzzleId(Difficulty.EASY, 42)

    @Test(timeout = 5000)
    fun generatedGamesArePlayableUniqueAndStartWithoutPlayerEntries() {
        val state = NewGameFactory.create(puzzle, false).state
        assertFalse(state.isComplete)
        assertFalse(state.hasEntries)
        assertEquals(GeneratorConfig.TARGET_CLUES, state.puzzle.cells.count { it != 0 })
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
