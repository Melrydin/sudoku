package sudoku.data.game

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sudoku.data.puzzle.SamplePuzzle
import sudoku.domain.common.OptionalValue
import sudoku.domain.game.GameSession
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.PuzzleId
import sudoku.domain.settings.GameOptions

@RunWith(AndroidJUnit4::class)
class SharedPreferencesGameRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val preferences: SharedPreferences = context.getSharedPreferences("games", Context.MODE_PRIVATE)
    private val store: SharedPreferencesGameRepository = SharedPreferencesGameRepository(context)
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
            previousGames.entries.forEach { entry: Map.Entry<String, *> -> putString(entry.key, entry.value as String) }
        }
    }

    @Test
    fun levelsAndDifficultiesHaveIndependentProgressAndTimers() {
        val first: PuzzleId = PuzzleId(Difficulty.EASY, 1)
        val second: PuzzleId = PuzzleId(Difficulty.EASY, 2)
        store.save(first, editedGame(4, 1_200))
        store.save(second, editedGame(1, 2_300))
        assertEquals(4, store.load(first).get().state.board[2])
        assertEquals(1, store.load(second).get().state.board[2])
        assertEquals(1_200L, store.load(first).get().elapsedMillis)
        assertEquals(2_300L, store.load(second).get().elapsedMillis)
        assertEquals(OptionalValue.Absent, store.load(PuzzleId(Difficulty.MEDIUM, 1)))
        assertEquals(setOf(6), store.load(first).get().state.notes[3])
        assertTrue(store.load(first).get().canUndo)
    }

    @Test
    fun legacyProgressIsMigratedOnlyIntoVersionOneLevelOne() {
        preferences.edit {
            putString(
                Difficulty.EASY.name,
                GameStateCodec.encode(editedGame(4, 1_200))
            )
        }
        assertEquals(OptionalValue.Absent, store.load(PuzzleId(Difficulty.EASY, 2)))
        assertTrue(preferences.contains(Difficulty.EASY.name))
        val first: PuzzleId = PuzzleId(Difficulty.EASY, 1)
        val restored: GameSession = store.load(first).get()
        assertEquals(4, restored.state.board[2])
        assertEquals(setOf(6), restored.state.notes[3])
        assertEquals(1_200L, restored.elapsedMillis)
        assertTrue(restored.canUndo)
        assertTrue(preferences.contains(first.key))
        assertFalse(preferences.contains(Difficulty.EASY.name))
    }

    @Test
    fun existingLevelProgressIsNeverOverwrittenByLegacyData() {
        val first: PuzzleId = PuzzleId(Difficulty.EASY, 1)
        store.save(first, editedGame(4, 0))
        preferences.edit {
            putString(
                Difficulty.EASY.name,
                GameStateCodec.encode(editedGame(1, 0))
            )
        }
        assertEquals(4, store.load(first).get().state.board[2])
    }

    @Test
    fun malformedCurrentProgressDoesNotSilentlyRestoreAnOlderGame() {
        val first: PuzzleId = PuzzleId(Difficulty.EASY, 1)
        preferences.edit {
            putString(first.key, "broken json")
            putString(Difficulty.EASY.name, GameStateCodec.encode(editedGame(1, 0)))
        }
        assertEquals(OptionalValue.Absent, store.load(first))
    }

    @Test
    fun restoredPrefillSettingPreservesManualChangesAndAllowsReenabling() {
        val game: GameSession = GameSession(SamplePuzzle.create())
        val options: GameOptions = GameOptions(prefillNotes = true)
        game.applyOptions(options)
        game.select(2)
        game.erase()
        val restored: GameSession = GameStateCodec.decode(GameStateCodec.encode(game)).get()
        restored.applyOptions(options)
        assertTrue(restored.state.notes[2].isEmpty())
        restored.applyOptions(options.copy(prefillNotes = false))
        val disabled: GameSession = GameStateCodec.decode(GameStateCodec.encode(restored)).get()
        disabled.applyOptions(options)
        assertEquals(setOf(1, 2, 4), disabled.state.notes[2])
    }

    @Test
    fun olderPrefilledSavesPreserveManualNoteChanges() {
        val game: GameSession = GameSession(SamplePuzzle.create())
        val options: GameOptions = GameOptions(prefillNotes = true)
        game.applyOptions(options)
        game.select(2)
        game.erase()
        val legacy: JSONObject =
            org.json.JSONObject(GameStateCodec.encode(game)).apply { remove("prefillNotesEnabled") }
        val restored: GameSession = GameStateCodec.decode(legacy.toString()).get()
        restored.applyOptions(options)
        assertTrue(restored.state.notes[2].isEmpty())
    }

    private fun editedGame(digit: Int, elapsed: Long): GameSession =
        GameSession(SamplePuzzle.create()).apply {
            select(2)
            enter(digit)
            select(3)
            toggleNotes()
            enter(6)
            trackElapsedTime(elapsed)
        }
}
