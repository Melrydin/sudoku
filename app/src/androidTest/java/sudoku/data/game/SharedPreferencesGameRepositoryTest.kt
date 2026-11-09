package sudoku.data.game

import android.content.Context
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sudoku.data.puzzle.SamplePuzzle
import sudoku.domain.game.GameSession
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.PuzzleId

@RunWith(AndroidJUnit4::class)
class SharedPreferencesGameRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val preferences = context.getSharedPreferences("games", Context.MODE_PRIVATE)
    private val store = SharedPreferencesGameRepository(context)
    private var previousGames: Map<String, *> = emptyMap<String, String>()

    @Before
    fun setUp() {
        previousGames = preferences.all.toMap()
        preferences.edit { clear() }
    }

    @After
    fun tearDown() {
        preferences.edit {
            clear()
            previousGames.forEach { (key, value) -> putString(key, value as String) }
        }
    }

    @Test
    fun levelsAndDifficultiesHaveIndependentProgressAndTimers() {
        val first = PuzzleId(Difficulty.EASY, 1)
        val second = PuzzleId(Difficulty.EASY, 2)
        store.save(first, editedGame(4, 1_200))
        store.save(second, editedGame(1, 2_300))
        assertEquals(4, store.load(first)!!.state.board[2])
        assertEquals(1, store.load(second)!!.state.board[2])
        assertEquals(1_200L, store.load(first)!!.elapsedMillis)
        assertEquals(2_300L, store.load(second)!!.elapsedMillis)
        assertNull(store.load(PuzzleId(Difficulty.MEDIUM, 1)))
        assertEquals(setOf(6), store.load(first)!!.state.notes[3])
        assertTrue(store.load(first)!!.canUndo)
    }

    @Test
    fun legacyProgressIsMigratedOnlyIntoVersionOneLevelOne() {
        preferences.edit { putString(Difficulty.EASY.name, GameStateCodec.encode(editedGame(4, 1_200))) }
        assertNull(store.load(PuzzleId(Difficulty.EASY, 2)))
        assertTrue(preferences.contains(Difficulty.EASY.name))
        val first = PuzzleId(Difficulty.EASY, 1)
        val restored = requireNotNull(store.load(first))
        assertEquals(4, restored.state.board[2])
        assertEquals(setOf(6), restored.state.notes[3])
        assertEquals(1_200L, restored.elapsedMillis)
        assertTrue(restored.canUndo)
        assertTrue(preferences.contains(first.key))
        assertFalse(preferences.contains(Difficulty.EASY.name))
    }

    @Test
    fun existingLevelProgressIsNeverOverwrittenByLegacyData() {
        val first = PuzzleId(Difficulty.EASY, 1)
        store.save(first, editedGame(4, 0))
        preferences.edit { putString(Difficulty.EASY.name, GameStateCodec.encode(editedGame(1, 0))) }
        assertEquals(4, store.load(first)!!.state.board[2])
    }

    @Test
    fun malformedCurrentProgressDoesNotSilentlyRestoreAnOlderGame() {
        val first = PuzzleId(Difficulty.EASY, 1)
        preferences.edit {
            putString(first.key, "broken json")
            putString(Difficulty.EASY.name, GameStateCodec.encode(editedGame(1, 0)))
        }
        assertNull(store.load(first))
    }

    private fun editedGame(digit: Int, elapsed: Long): GameSession = GameSession(SamplePuzzle.create()).apply {
        select(2)
        enter(digit)
        select(3)
        toggleNotes()
        enter(6)
        trackElapsedTime(elapsed)
    }
}
