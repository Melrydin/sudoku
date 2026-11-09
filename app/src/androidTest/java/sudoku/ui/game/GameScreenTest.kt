package sudoku.ui.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.SystemClock
import android.view.View
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.doubleClick
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sudoku.R
import sudoku.data.game.GameStateCodec
import sudoku.data.game.SharedPreferencesGameRepository
import sudoku.data.preferences.AppPreferences
import sudoku.data.preferences.GamePreferences
import sudoku.data.puzzle.SamplePuzzle
import sudoku.domain.game.GameNotes
import sudoku.domain.game.GameSession
import sudoku.domain.game.GameState
import sudoku.domain.generation.PuzzleRandom
import sudoku.domain.generation.SudokuGenerator
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.PuzzleId
import sudoku.domain.settings.ColorTheme
import sudoku.domain.settings.GameOptions
import sudoku.domain.solver.SolutionCount
import sudoku.domain.solver.SolutionCounter
import sudoku.testing.ScreenCapture
import sudoku.ui.game.GameActivity
import sudoku.ui.home.MainActivity

@RunWith(AndroidJUnit4::class)
class GameScreenTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val games by lazy { SharedPreferencesGameRepository(context) }
    private val easyPuzzle = PuzzleId(Difficulty.EASY)
    private val preferences = context.getSharedPreferences("games", Context.MODE_PRIVATE)
    private var previousGames: Map<String, *> = emptyMap<String, String>()
    private lateinit var scenario: ActivityScenario<GameActivity>
    private val homePreferences = AppPreferences(context)
    private lateinit var previousTheme: ColorTheme
    private lateinit var previousDifficulty: Difficulty
    private var previousEasyLevel: Int = 1
    private val gamePreferences = GamePreferences(context)
    private lateinit var previousOptions: GameOptions

    @Before
    fun setUp() {
        previousGames = preferences.all.toMap()
        previousTheme = homePreferences.theme
        previousDifficulty = homePreferences.difficulty
        previousOptions = gamePreferences.options
        gamePreferences.options = GameOptions()
        homePreferences.difficulty = Difficulty.EASY
        previousEasyLevel = homePreferences.level
        homePreferences.level = 1
        preferences.edit { clear() }
        games.save(easyPuzzle, GameSession(SamplePuzzle.create()))
        scenario = ActivityScenario.launch(GameActivity.intent(context, easyPuzzle, false))
    }

    @After
    fun tearDown() {
        scenario.close()
        preferences.edit {
            clear()
            previousGames.forEach { (key, value) -> putString(key, value as String) }
        }
        homePreferences.theme = previousTheme
        homePreferences.difficulty = Difficulty.EASY
        homePreferences.level = previousEasyLevel
        homePreferences.difficulty = previousDifficulty
        gamePreferences.options = previousOptions
    }

    @Test
    fun playerCanEnterEraseAndUndoWithoutEditingGivens() {
        cell(2).perform(click())
        number(4).perform(scrollTo(), click())
        cell(2).check(matches(withText("4")))
        onView(withId(R.id.erase)).perform(scrollTo(), click())
        cell(2).check(matches(withText("")))
        onView(withId(R.id.undo)).perform(scrollTo(), click())
        cell(2).check(matches(withText("4")))
        cell(0).perform(scrollTo(), click())
        number(9).perform(scrollTo(), click())
        cell(0).check(matches(withText("5")))
    }

    @Test
    fun notesProgressAndHistorySurviveRecreation() {
        cell(2).perform(click())
        onView(withId(R.id.notes)).perform(scrollTo(), click())
        number(4).perform(scrollTo(), click())
        scenario.recreate()
        cell(2).check(matches(withContentDescription("Zeile 1, Spalte 3, Notizen: 4, bearbeitbar")))
        onView(withId(R.id.notes)).check(matches(withContentDescription(R.string.notes_enabled)))
        onView(withId(R.id.undo)).perform(scrollTo(), click())
        assertTrue(games.load(easyPuzzle)!!.state.notes[2].isEmpty())
    }

    @Test
    fun restartRequiresConfirmationAndCancelKeepsProgress() {
        cell(2).perform(click())
        number(4).perform(scrollTo(), click())
        onView(withId(R.id.restart_game)).perform(scrollTo(), click())
        onView(withText(R.string.cancel)).perform(click())
        cell(2).check(matches(withText("4")))
        onView(withId(R.id.restart_game)).perform(scrollTo(), click())
        onView(withText(R.string.new_game)).perform(click())
        cell(2).check(matches(withText("")))
    }

    @Test
    fun aNewActivityResumesSavedNumbers() {
        cell(2).perform(click())
        number(4).perform(scrollTo(), click())
        scenario.close()
        scenario = ActivityScenario.launch(GameActivity.intent(context, easyPuzzle, false))
        cell(2).check(matches(withText("4")))
    }

    @Test
    fun savedGamesAreIndependentAndMalformedDataIsIgnored() {
        cell(2).perform(click())
        number(4).perform(scrollTo(), click())
        val store = games
        assertNotNull(store.load(easyPuzzle))
        assertNull(store.load(PuzzleId(Difficulty.HARD)))
        assertNull(GameStateCodec.decode("broken json"))
        assertEquals(4, store.load(easyPuzzle)!!.state.board[2])
    }

    @Test
    fun themeChangesPreserveTheBoard() {
        cell(2).perform(click())
        number(4).perform(scrollTo(), click())
        onView(withId(R.id.game_theme)).perform(click())
        onView(withContentDescription(R.string.theme_ocean)).perform(click())
        cell(2).check(matches(withText("4")))
        onView(withId(R.id.sudoku_board)).check(matches(isDisplayed()))
        ScreenCapture.capture("game-ocean.png")
    }

    @Test
    fun homeStartsAndResumesTheGame() {
        scenario.close()
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.new_game)).perform(click())
            waitForGame()
            val editable = games.load(easyPuzzle)!!.state.puzzle.cells.indexOf(0)
            cell(editable).perform(click())
            assertEquals(editable, games.load(easyPuzzle)!!.state.selectedCell)
            number(4).perform(scrollTo(), click())
            assertEquals(4, games.load(easyPuzzle)!!.state.board[editable])
            pressBack()
            assertEquals(4, games.load(easyPuzzle)!!.state.board[editable])
            onView(withId(R.id.continue_game)).check(matches(isDisplayed()))
            onView(withId(R.id.continue_game)).perform(click())
            cell(editable).check(matches(withText("4")))
            pressBack()
        }
    }

    @Test
    fun settingsUpdateTheRunningGameWithoutLosingEntries() {
        cell(2).perform(click())
        number(4).perform(scrollTo(), click())
        number(1).check(matches(withText("1\n6")))
        onView(withId(R.id.game_settings)).perform(scrollTo(), click())
        onView(withId(R.id.settings_assists)).perform(scrollTo(), click())
        onView(allOf(withId(R.id.setting_toggle), isDescendantOfA(withId(R.id.setting_remaining))))
            .perform(scrollTo(), click())
        pressBack()
        pressBack()
        number(1).check(matches(withText("1")))
        cell(2).check(matches(withText("4")))
    }

    @Test
    fun homeResumesOnlyTheSelectedLevel() {
        scenario.close()
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.next_level)).perform(click())
            val editable = startSelectedLevelAndEnter(4)
            pressBack()
            onView(withId(R.id.continue_game)).check(matches(isDisplayed()))
            onView(withId(R.id.next_level)).perform(click())
            onView(withId(R.id.continue_game)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
            onView(withId(R.id.previous_level)).perform(click())
            onView(withId(R.id.continue_game)).perform(click())
            cell(editable).check(matches(withText("4")))
            pressBack()
        }
    }

    @Test
    fun prefillingOnlyAffectsNewGamesAndSurvivesRecreation() {
        gamePreferences.options = GameOptions(prefillNotes = true)
        scenario.close()
        scenario = ActivityScenario.launch(GameActivity.intent(context, easyPuzzle, true))
        waitForGame()
        val store = games
        val puzzle = store.load(easyPuzzle)!!.state.puzzle
        val editable = puzzle.cells.indexOf(0)
        assertEquals(GameNotes.prefill(puzzle), store.load(easyPuzzle)!!.state.notes)
        cell(editable).perform(click())
        onView(withId(R.id.erase)).perform(scrollTo(), click())
        scenario.recreate()
        assertTrue(store.load(easyPuzzle)!!.state.notes[editable].isEmpty())
    }

    @Test
    fun ninthDigitDisappearsAndUndoBringsItsButtonBack() {
        scenario.close()
        val puzzle = SamplePuzzle.create().puzzle
        val board = puzzle.cells.indices.filter { puzzle[it] == 0 }.take(5)
            .fold(puzzle) { current, index -> current.withValue(index, 5) }
        games.save(easyPuzzle, GameSession(GameState(puzzle, board)))
        scenario = ActivityScenario.launch(GameActivity.intent(context, easyPuzzle, false))
        cell(8).perform(click())
        number(5).perform(scrollTo(), click())
        number(5).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
        onView(withId(R.id.undo)).perform(scrollTo(), click())
        number(5).check(matches(withText("5\n1")))
        number(5).check(matches(isDisplayed()))
    }

    @Test
    fun optionalEraseGesturesSupportUndoAndProtectGivens() {
        gamePreferences.options = GameOptions(doubleTapErase = true, holdToErase = true)
        scenario.recreate()
        cell(2).perform(click())
        number(4).perform(scrollTo(), click())
        cell(2).perform(scrollTo(), doubleClick())
        cell(2).check(matches(withText("")))
        onView(withId(R.id.undo)).perform(scrollTo(), click())
        cell(2).perform(scrollTo(), longClick())
        cell(2).check(matches(withText("")))
        cell(0).perform(doubleClick(), longClick())
        cell(0).check(matches(withText("5")))
    }

    @Test
    fun timerPausesWithTheActivityAndPersistsElapsedTime() {
        SystemClock.sleep(1_200)
        scenario.moveToState(Lifecycle.State.STARTED)
        val store = games
        val paused = requireNotNull(store.load(easyPuzzle)).elapsedMillis
        assertTrue(paused >= 1_000)
        SystemClock.sleep(1_200)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.moveToState(Lifecycle.State.STARTED)
        val resumed = requireNotNull(store.load(easyPuzzle)).elapsedMillis
        assertTrue(resumed in paused..(paused + 1_000))
    }

    @Test
    fun codecPreservesPlayTimeAndReadsOlderGames() {
        val game = GameSession(SamplePuzzle.create(), initialElapsedMillis = 123_456)
        val encoded = GameStateCodec.encode(game)
        assertEquals(123_456L, requireNotNull(GameStateCodec.decode(encoded)).elapsedMillis)
        val oldSave = org.json.JSONObject(encoded).apply { remove("elapsedMillis") }.toString()
        assertEquals(0L, requireNotNull(GameStateCodec.decode(oldSave)).elapsedMillis)
    }

    @Test
    fun launcherIconRendersAsAnAdaptiveSudokuIcon() {
        val icon = context.packageManager.getApplicationIcon(context.packageName)
        assertTrue(icon is AdaptiveIconDrawable)
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        icon.setBounds(0, 0, bitmap.width, bitmap.height)
        icon.draw(Canvas(bitmap))
        ScreenCapture.save("app-icon.png", bitmap)
    }

    @Test
    fun newGamesUseUniqueGeneratedPuzzlesAndSurviveRecreation() {
        scenario.close()
        scenario = ActivityScenario.launch(GameActivity.intent(context, easyPuzzle, true))
        scenario.recreate()
        waitForGame()
        val state = games.load(easyPuzzle)!!.state
        assertEquals(SolutionCount.UNIQUE, SolutionCounter().count(state.puzzle))
        assertTrue(state.puzzle.cells != SamplePuzzle.create().puzzle.cells)
        assertTrue(!state.isComplete && !state.hasEntries)
        onView(withId(R.id.sample_puzzle_label)).check(matches(withText(R.string.unrated_puzzle)))
        checkHeaderBelowStatusBar()
        ScreenCapture.capture("generated-game.png")
    }

    @Test
    fun completingAGeneratedPuzzleShowsTheCompletionMessage() {
        scenario.close()
        scenario = ActivityScenario.launch(GameActivity.intent(context, easyPuzzle, true))
        waitForGame()
        val puzzle = games.load(easyPuzzle)!!.state.puzzle
        val solution = SudokuGenerator(PuzzleRandom.forPuzzle(easyPuzzle)).generate()
        val editable = puzzle.cells.indexOf(0)
        scenario.close()
        games.save(easyPuzzle, GameSession(GameState(puzzle, solution.withValue(editable, 0))))
        scenario = ActivityScenario.launch(GameActivity.intent(context, easyPuzzle, false))
        cell(editable).perform(click())
        number(solution[editable]).perform(scrollTo(), click())
        onView(withId(R.id.game_completed)).check(matches(isDisplayed()))
        assertTrue(games.load(easyPuzzle)!!.state.isComplete)
    }

    private fun startSelectedLevelAndEnter(value: Int): Int {
        onView(withId(R.id.new_game)).perform(click())
        waitForGame()
        val editable = games.load(homePreferences.puzzleId)!!.state.puzzle.cells.indexOf(0)
        cell(editable).perform(click())
        number(value).perform(scrollTo(), click())
        return editable
    }

    private fun checkHeaderBelowStatusBar() {
        scenario.onActivity { activity ->
            val header = activity.findViewById<View>(R.id.game_header)
            val position = IntArray(2)
            header.getLocationOnScreen(position)
            val insets = requireNotNull(ViewCompat.getRootWindowInsets(header))
            assertTrue(position[1] >= insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
        }
    }

    private fun cell(index: Int) = onView(withTagValue(equalTo("cell_$index")))

    private fun number(value: Int) = onView(withTagValue(equalTo("number_$value")))

}
