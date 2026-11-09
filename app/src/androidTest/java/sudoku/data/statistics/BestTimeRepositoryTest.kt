package sudoku.data.statistics

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.PuzzleId
import sudoku.domain.statistics.GameResult
import sudoku.testing.PreferenceSnapshot

@RunWith(AndroidJUnit4::class)
class BestTimeRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val store = SharedPreferencesBestTimeRepository(context)
    private val snapshot = PreferenceSnapshot(context, "best_times")

    @Before
    fun setUp() = snapshot.captureAndClear()

    @After
    fun tearDown() = snapshot.restore()

    @Test
    fun ranksByTimeBeforeAssistancePoints() {
        store.record(result("slow", 60_000, 0))
        store.record(result("fast", 10_000, 5))
        val entries = store.forDifficulty(Difficulty.EASY)
        assertEquals(listOf("fast", "slow"), entries.map { it.gameId })
        assertEquals(listOf(5, 0), entries.map { it.assistancePoints })
    }

    @Test
    fun aGameCanOnlyBeRecordedOnce() {
        val first = result("same-game", 60_000, 4)
        store.record(first)
        store.record(result("same-game", 5_000, 0))
        assertEquals(first, store.find("same-game"))
        assertEquals(1, store.forDifficulty(Difficulty.EASY).size)
    }

    @Test
    fun rankingsAreSeparatedByDifficultyAndKeepTheirLevel() {
        store.record(result("easy", 60_000, 4))
        store.record(result("hard", 20_000, 1).copy(puzzle = PuzzleId(Difficulty.HARD, 7)))
        assertEquals(listOf("easy"), store.forDifficulty(Difficulty.EASY).map { it.gameId })
        assertEquals(7, store.forDifficulty(Difficulty.HARD).single().puzzle.level)
    }

    @Test
    fun resultsSurviveCreatingANewRepository() {
        val record = result("saved", 125_456, 3)
        store.record(record)
        assertEquals(record, SharedPreferencesBestTimeRepository(context).find(record.gameId))
    }

    @Test
    fun malformedResultsAreIgnoredWithoutHidingValidRecords() {
        context.getSharedPreferences("best_times", Context.MODE_PRIVATE).edit().putString("broken", "oops").commit()
        store.record(result("valid", 20_000, 1))
        assertNull(store.find("broken"))
        assertEquals(listOf("valid"), store.forDifficulty(Difficulty.EASY).map { it.gameId })
    }

    private fun result(id: String, elapsed: Long, points: Int): GameResult =
        GameResult(id, PuzzleId(Difficulty.EASY, 2), elapsed, points, 123_456)
}
