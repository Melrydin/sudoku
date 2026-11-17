package sudoku.domain.game

import org.junit.Assert.assertEquals
import org.junit.Test
import sudoku.domain.common.OptionalValue
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.GeneratorVersion
import sudoku.domain.puzzle.PuzzleId
import sudoku.fixtures.TestBoards

class SavedPuzzleTest {
    private val current: PuzzleId = PuzzleId(Difficulty.EASY, 42, GeneratorVersion.V2)
    private val legacy: PuzzleId = current.copy(version = GeneratorVersion.V1)

    @Test
    fun newestSavedVersionWinsWithoutReplacingOlderProgress() {
        val store: GameRepository = repositoryWith(legacy, current)
        assertEquals(current, store.findSavedPuzzle(current).get())
        assertEquals(legacy, store.findSavedPuzzle(legacy).get())
    }

    @Test
    fun olderProgressCanBeFoundForTheSelectedLevelAndDifficulty() {
        val store: GameRepository = repositoryWith(legacy)
        assertEquals(legacy, store.findSavedPuzzle(current).get())
        assertEquals(OptionalValue.Absent, store.findSavedPuzzle(current.copy(level = 43)))
        assertEquals(OptionalValue.Absent, store.findSavedPuzzle(current.copy(difficulty = Difficulty.MEDIUM)))
    }

    @Test
    fun versionOneNeverLoadsVersionTwoProgress() {
        assertEquals(OptionalValue.Absent, repositoryWith(current).findSavedPuzzle(legacy))
    }

    private fun repositoryWith(vararg puzzles: PuzzleId): GameRepository = object : GameRepository {
        override fun load(puzzle: PuzzleId): OptionalValue<GameSession> =
            if (puzzle in puzzles) OptionalValue.Present(GameSession(GameState(TestBoards.solved())))
            else OptionalValue.Absent

        override fun save(puzzle: PuzzleId, session: GameSession): Unit = Unit

        override fun delete(puzzle: PuzzleId): Unit = Unit
    }
}
