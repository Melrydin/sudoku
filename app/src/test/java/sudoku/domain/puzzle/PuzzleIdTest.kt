package sudoku.domain.puzzle

import org.junit.Assert.assertEquals
import org.junit.Test

class PuzzleIdTest {
    @Test
    fun saveKeysUseVersionStableDifficultyIdsAndLevel() {
        assertEquals("v1:easy:42", PuzzleId(Difficulty.EASY, 42).key)
        assertEquals("v1:medium:42", PuzzleId(Difficulty.MEDIUM, 42).key)
        assertEquals("v1:hard:42", PuzzleId(Difficulty.HARD, 42).key)
        assertEquals("v1:expert:42", PuzzleId(Difficulty.EXTREME, 42).key)
    }

    @Test(expected = IllegalArgumentException::class)
    fun levelZeroCannotBeUsedAsASaveSlot() {
        PuzzleId(Difficulty.EASY, 0)
    }
}
