package sudoku.ui.game

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sudoku.R
import sudoku.data.game.SharedPreferencesGameRepository
import sudoku.data.preferences.AppPreferences
import sudoku.data.preferences.GamePreferences
import sudoku.data.puzzle.SamplePuzzle
import sudoku.data.statistics.SharedPreferencesBestTimeRepository
import sudoku.domain.game.GameSession
import sudoku.domain.game.GameState
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.PuzzleId
import sudoku.domain.settings.GameOptions
import sudoku.testing.PreferenceSnapshot
import sudoku.testing.ScreenCapture
import sudoku.testing.finishTestActivities
import sudoku.ui.common.PlayTimeFormatter

@RunWith(AndroidJUnit4::class)
class GameOutcomeTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val snapshot = PreferenceSnapshot(context, "games", "best_times", "home", "settings")
    private val games = SharedPreferencesGameRepository(context)
    private val bestTimes = SharedPreferencesBestTimeRepository(context)
    private val puzzle = PuzzleId(Difficulty.EASY)
    private lateinit var scenario: ActivityScenario<GameActivity>

    @Before
    fun setUp() {
        snapshot.captureAndClear()
        AppPreferences(context).difficulty = Difficulty.EASY
        GamePreferences(context).options = GameOptions()
        games.save(puzzle, GameSession(SamplePuzzle.create()))
        scenario = ActivityScenario.launch(GameActivity.intent(context, puzzle, false))
    }

    @After
    fun tearDown() {
        scenario.close()
        finishTestActivities()
        snapshot.restore()
    }

    @Test
    fun livesSurviveRecreationAndUndoCannotRefundThem() {
        cell(2).perform(click())
        number(1).perform(scrollTo(), click())
        checkLives(2)
        scenario.recreate()
        checkLives(2)
        onView(withId(R.id.undo)).perform(scrollTo(), click())
        checkLives(2)
        cell(2).check(matches(withText("")))
    }

    @Test
    fun theThirdMistakeDeletesOnlyThisLevelAndReturnsToAFreshStart() {
        games.save(puzzle.copy(level = 2), GameSession(SamplePuzzle.create()))
        loseGame()
        onView(withText(R.string.game_over)).check(matches(isDisplayed()))
        assertNull(games.load(puzzle))
        assertNotNull(games.load(puzzle.copy(level = 2)))
        assertEquals(0, bestTimes.forDifficulty(Difficulty.EASY).size)
        scenario.recreate()
        onView(withText(R.string.game_over_message)).check(matches(isDisplayed()))
        ScreenCapture.capture("game-over.png")
        onView(withText(R.string.back_home)).perform(click())
        onView(withId(R.id.continue_game)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
        onView(withId(R.id.new_game)).perform(click())
        waitForGame()
        checkLives(3)
        assertFalse(games.load(puzzle)!!.state.hasEntries)
    }

    @Test
    fun victoryShowsTheRecordedTimeAndAssistanceAndOpensTheRanking() {
        val game = prepareWinningGame()
        number(4).perform(scrollTo(), click())
        val result = requireNotNull(bestTimes.find(game.id))
        val time = PlayTimeFormatter.format(result.elapsedMillis)
        onView(withId(R.id.victory_time)).check(matches(withText(time)))
        val assistance = context.getString(R.string.assistance_points, 4, 5)
        onView(withId(R.id.victory_assistance)).check(matches(withText(assistance)))
        assertEquals(1, bestTimes.forDifficulty(Difficulty.EASY).size)
        ScreenCapture.capture("victory.png")
        onView(withId(R.id.victory_best_times)).perform(scrollTo(), click())
        onView(withId(R.id.result_time)).check(matches(withText(time)))
        onView(withId(R.id.result_level)).check(matches(withText(context.getString(R.string.level_number, 1))))
        ScreenCapture.capture("best-times.png")
    }

    @Test
    fun reopeningACompletedGameDoesNotDuplicateItsResult() {
        val game = prepareWinningGame()
        number(4).perform(scrollTo(), click())
        val first = bestTimes.find(game.id)
        scenario = ActivityScenario.launch(GameActivity.intent(context, puzzle, false))
        onView(withId(R.id.victory_title)).check(matches(isDisplayed()))
        assertEquals(1, bestTimes.forDifficulty(Difficulty.EASY).size)
        assertEquals(first, bestTimes.find(game.id))
    }

    private fun prepareWinningGame(): GameSession {
        scenario.close()
        val original = GameSession(SamplePuzzle.create())
        val state = GameState(original.state.puzzle, original.solution.withValue(2, 0), selectedCell = 2)
        val game = GameSession(state, initialElapsedMillis = 125_000, initialSolution = original.solution)
        games.save(puzzle, game)
        scenario = ActivityScenario.launch(GameActivity.intent(context, puzzle, false))
        return game
    }

    private fun loseGame() {
        cell(2).perform(click())
        repeat(3) { number(1).perform(scrollTo(), click()) }
    }

    private fun checkLives(lives: Int) {
        onView(withId(R.id.game_lives)).check(matches(withText(context.getString(R.string.game_lives, lives, 3))))
    }

    private fun cell(index: Int) = onView(withTagValue(equalTo("cell_$index")))

    private fun number(value: Int) = onView(withTagValue(equalTo("number_$value")))
}
