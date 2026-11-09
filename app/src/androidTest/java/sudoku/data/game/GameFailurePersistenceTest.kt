package sudoku.data.game

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
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
import sudoku.testing.PreferenceSnapshot

@RunWith(AndroidJUnit4::class)
class GameFailurePersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val store = SharedPreferencesGameRepository(context)
    private val snapshot = PreferenceSnapshot(context, "games")
    private val puzzle = PuzzleId(Difficulty.EASY)

    @Before
    fun setUp() = snapshot.captureAndClear()

    @After
    fun tearDown() = snapshot.restore()

    @Test
    fun livesAndSolutionSurviveSavingAndUndo() {
        val session = GameSession(SamplePuzzle.create())
        session.select(2)
        session.enter(1)
        store.save(puzzle, session)
        val restored = requireNotNull(store.load(puzzle))
        restored.undo()
        assertEquals(2, restored.remainingLives)
        assertEquals(session.solution.cells, restored.solution.cells)
    }

    @Test
    fun aFailedGameIsDeletedAndCannotBeSavedAgain() {
        val session = GameSession(SamplePuzzle.create())
        store.save(puzzle, session)
        session.select(2)
        repeat(3) { session.enter(1) }
        store.save(puzzle, session)
        assertNull(store.load(puzzle))
        store.save(puzzle, session)
        assertNull(store.load(puzzle))
    }

    @Test
    fun deletingTheCurrentLevelAlsoPreventsLegacyResurrection() {
        val preferences = context.getSharedPreferences("games", android.content.Context.MODE_PRIVATE)
        val other = puzzle.copy(level = 2)
        store.save(puzzle, GameSession(SamplePuzzle.create()))
        store.save(other, GameSession(SamplePuzzle.create()))
        preferences.edit().putString(Difficulty.EASY.name, GameStateCodec.encode(store.load(other)!!)).commit()
        store.delete(puzzle)
        assertNull(store.load(puzzle))
        assertFalse(preferences.contains(Difficulty.EASY.name))
        assertTrue(store.load(other) != null)
    }

    @Test
    fun olderSavesReceiveThreeLivesAndAReferenceSolution() {
        val json = JSONObject(GameStateCodec.encode(GameSession(SamplePuzzle.create())))
        listOf("solution", "mistakes").forEach { json.remove(it) }
        val restored = requireNotNull(GameStateCodec.decode(json.toString()))
        assertEquals(3, restored.remainingLives)
        assertEquals(4, restored.solution[2])
    }

    @Test
    fun corruptedLivesAndReferenceSolutionsAreRejected() {
        val json = JSONObject(GameStateCodec.encode(GameSession(SamplePuzzle.create())))
        json.put("mistakes", 4)
        assertNull(GameStateCodec.decode(json.toString()))
        json.put("mistakes", 0).put("solution", "0".repeat(81))
        assertNull(GameStateCodec.decode(json.toString()))
    }
}
