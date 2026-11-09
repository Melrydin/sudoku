package com.example.sudoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.core.content.edit
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.doubleClick
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sudoku.game.GameActivity
import com.example.sudoku.game.GameStateCodec
import com.example.sudoku.game.GameStore
import com.example.sudoku.game.GameSession
import com.example.sudoku.game.GameNotes
import com.example.sudoku.game.GameOptions
import com.example.sudoku.game.SamplePuzzle
import com.example.sudoku.home.Difficulty
import com.example.sudoku.home.HomePreferences
import com.example.sudoku.home.HomeTheme
import com.example.sudoku.settings.GamePreferences
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val preferences = context.getSharedPreferences("games", Context.MODE_PRIVATE)
    private var previousGames: Map<String, *> = emptyMap<String, String>()
    private lateinit var scenario: ActivityScenario<GameActivity>
    private val homePreferences = HomePreferences(context)
    private lateinit var previousTheme: HomeTheme
    private lateinit var previousDifficulty: Difficulty
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
        preferences.edit { clear() }
        scenario = ActivityScenario.launch(GameActivity.intent(context, Difficulty.EASY, true))
    }

    @After
    fun tearDown() {
        scenario.close()
        preferences.edit {
            clear()
            previousGames.forEach { (key, value) -> putString(key, value as String) }
        }
        homePreferences.theme = previousTheme
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
        assertTrue(GameStore(context).load(Difficulty.EASY)!!.state.notes[2].isEmpty())
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
        scenario = ActivityScenario.launch(GameActivity.intent(context, Difficulty.EASY, false))
        cell(2).check(matches(withText("4")))
    }

    @Test
    fun savedGamesAreIndependentAndMalformedDataIsIgnored() {
        cell(2).perform(click())
        number(4).perform(scrollTo(), click())
        val store = GameStore(context)
        assertNotNull(store.load(Difficulty.EASY))
        assertNull(store.load(Difficulty.HARD))
        assertNull(GameStateCodec.decode("broken json"))
        assertEquals(4, store.load(Difficulty.EASY)!!.state.board[2])
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
            cell(2).perform(click())
            number(4).perform(scrollTo(), click())
            assertEquals(4, GameStore(context).load(Difficulty.EASY)!!.state.board[2])
            pressBack()
            assertEquals(4, GameStore(context).load(Difficulty.EASY)!!.state.board[2])
            onView(withId(R.id.continue_game)).check(matches(isDisplayed()))
            onView(withId(R.id.continue_game)).perform(click())
            cell(2).check(matches(withText("4")))
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
    fun prefillingOnlyAffectsNewGamesAndSurvivesRecreation() {
        gamePreferences.options = GameOptions(prefillNotes = true)
        scenario.close()
        scenario = ActivityScenario.launch(GameActivity.intent(context, Difficulty.EASY, true))
        val store = GameStore(context)
        assertEquals(GameNotes.prefill(SamplePuzzle.create().board), store.load(Difficulty.EASY)!!.state.notes)
        cell(2).perform(click())
        onView(withId(R.id.erase)).perform(scrollTo(), click())
        scenario.recreate()
        assertTrue(store.load(Difficulty.EASY)!!.state.notes[2].isEmpty())
    }

    @Test
    fun ninthDigitDisappearsAndUndoBringsItsButtonBack() {
        scenario.close()
        val puzzle = SamplePuzzle.create().puzzle
        val board = puzzle.cells.indices.filter { puzzle[it] == 0 }.take(5)
            .fold(puzzle) { current, index -> current.withValue(index, 5) }
        GameStore(context).save(Difficulty.EASY, GameSession(com.example.sudoku.game.GameState(puzzle, board)))
        scenario = ActivityScenario.launch(GameActivity.intent(context, Difficulty.EASY, false))
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
        val store = GameStore(context)
        val paused = requireNotNull(store.load(Difficulty.EASY)).elapsedMillis
        assertTrue(paused >= 1_000)
        SystemClock.sleep(1_200)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.moveToState(Lifecycle.State.STARTED)
        val resumed = requireNotNull(store.load(Difficulty.EASY)).elapsedMillis
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

    private fun cell(index: Int) = onView(withTagValue(equalTo("cell_$index")))

    private fun number(value: Int) = onView(withTagValue(equalTo("number_$value")))

}
