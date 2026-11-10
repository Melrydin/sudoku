package sudoku.domain.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.GeneratorVersion
import sudoku.domain.puzzle.PuzzleId
import sudoku.fixtures.TestBoards

class SavedPuzzleTest {
    private val current = PuzzleId(Difficulty.EASY, 42, GeneratorVersion.V2)
    private val legacy = current.copy(version = GeneratorVersion.V1)

    @Test
    fun newestSavedVersionWinsWithoutReplacingOlderProgress() {
        val store = repositoryWith(legacy, current)
        assertEquals(current, store.findSavedPuzzle(current))
        assertEquals(legacy, store.findSavedPuzzle(legacy))
    }

    @Test
    fun olderProgressCanBeFoundForTheSelectedLevelAndDifficulty() {
        val store = repositoryWith(legacy)
        assertEquals(legacy, store.findSavedPuzzle(current))
        assertNull(store.findSavedPuzzle(current.copy(level = 43)))
        assertNull(store.findSavedPuzzle(current.copy(difficulty = Difficulty.MEDIUM)))
    }

    @Test
    fun versionOneNeverLoadsVersionTwoProgress() {
        assertNull(repositoryWith(current).findSavedPuzzle(legacy))
    }

    private fun repositoryWith(vararg puzzles: PuzzleId): GameRepository = object : GameRepository {
        override fun load(puzzle: PuzzleId): GameSession? =
            if (puzzle in puzzles) GameSession(GameState(TestBoards.solved())) else null

        override fun save(puzzle: PuzzleId, session: GameSession) = Unit

        override fun delete(puzzle: PuzzleId) = Unit
    }
}
