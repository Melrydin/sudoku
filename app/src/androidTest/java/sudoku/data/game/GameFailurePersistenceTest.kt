package sudoku.data.game

import android.content.Context
import android.content.SharedPreferences
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
import sudoku.testing.PreferenceSnapshot

@RunWith(AndroidJUnit4::class)
class GameFailurePersistenceTest {
    private val context: Context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val store: SharedPreferencesGameRepository = SharedPreferencesGameRepository(context)
    private val snapshot: PreferenceSnapshot = PreferenceSnapshot(context, "games")
    private val puzzle: PuzzleId = PuzzleId(Difficulty.EASY)

    @Before
    fun setUp(): Unit = snapshot.captureAndClear()

    @After
    fun tearDown(): Unit = snapshot.restore()

    @Test
    fun livesSolutionIdentityAndAssistanceSurviveSavingAndUndo() {
        val session: GameSession = GameSession(SamplePuzzle.create())
        session.select(2)
        session.enter(1)
        session.recordAssistance(GameOptions())
        store.save(puzzle, session)
        val restored: GameSession = store.load(puzzle).get()
        restored.undo()
        assertEquals(2, restored.remainingLives)
        assertEquals(session.solution.cells, restored.solution.cells)
        assertEquals(session.id, restored.id)
        assertEquals(4, restored.assistancePoints)
    }

    @Test
    fun aFailedGameIsDeletedAndCannotBeSavedAgain() {
        val session: GameSession = GameSession(SamplePuzzle.create())
        store.save(puzzle, session)
        session.select(2)
        repeat(3) { session.enter(1) }
        store.save(puzzle, session)
        assertEquals(OptionalValue.Absent, store.load(puzzle))
        store.save(puzzle, session)
        assertEquals(OptionalValue.Absent, store.load(puzzle))
    }

    @Test
    fun deletingTheCurrentLevelAlsoPreventsLegacyResurrection() {
        val preferences: SharedPreferences =
            context.getSharedPreferences("games", android.content.Context.MODE_PRIVATE)
        val other: PuzzleId = puzzle.copy(level = 2)
        store.save(puzzle, GameSession(SamplePuzzle.create()))
        store.save(other, GameSession(SamplePuzzle.create()))
        preferences.edit()
            .putString(Difficulty.EASY.name, GameStateCodec.encode(store.load(other).get())).commit()
        store.delete(puzzle)
        assertEquals(OptionalValue.Absent, store.load(puzzle))
        assertFalse(preferences.contains(Difficulty.EASY.name))
        assertTrue(store.load(other).isPresent)
    }

    @Test
    fun olderSavesReceiveThreeLivesAndAReferenceSolution() {
        val json: JSONObject = JSONObject(GameStateCodec.encode(GameSession(SamplePuzzle.create())))
        listOf("solution", "mistakes", "id", "assistance").forEach { json.remove(it) }
        val restored: GameSession = GameStateCodec.decode(json.toString()).get()
        assertEquals(3, restored.remainingLives)
        assertEquals(4, restored.solution[2])
        assertEquals(0, restored.assistancePoints)
    }

    @Test
    fun corruptedLivesAndReferenceSolutionsAreRejected() {
        val json: JSONObject = JSONObject(GameStateCodec.encode(GameSession(SamplePuzzle.create())))
        json.put("mistakes", 4)
        assertEquals(OptionalValue.Absent, GameStateCodec.decode(json.toString()))
        json.put("mistakes", 0).put("solution", "0".repeat(81))
        assertEquals(OptionalValue.Absent, GameStateCodec.decode(json.toString()))
    }
}
