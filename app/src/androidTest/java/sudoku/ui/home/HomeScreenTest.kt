package sudoku.ui.home

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sudoku.R
import sudoku.data.preferences.AppPreferences
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.settings.ColorTheme
import sudoku.testing.ScreenCapture

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    private val preferences = AppPreferences(ApplicationProvider.getApplicationContext())
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var previousTheme: ColorTheme
    private lateinit var previousDifficulty: Difficulty
    private var previousLevels: Map<Difficulty, Int> = emptyMap()

    @Before
    fun setUp() {
        previousTheme = preferences.theme
        previousDifficulty = preferences.difficulty
        previousLevels = resetLevels()
        preferences.theme = ColorTheme.AURUM
        preferences.difficulty = Difficulty.EASY
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
        previousLevels.forEach { (difficulty, level) ->
            preferences.difficulty = difficulty
            preferences.level = level
        }
        preferences.theme = previousTheme
        preferences.difficulty = previousDifficulty
    }

    @Test
    fun difficultySurvivesActivityRecreation() {
        onView(withId(R.id.next_difficulty)).perform(click())
        scenario.recreate()
        onView(withId(R.id.difficulty_name)).check(matches(withText(R.string.difficulty_medium)))
        ScreenCapture.capture("home.png")
    }

    @Test
    fun themeSelectionPreservesDifficulty() {
        onView(withId(R.id.next_difficulty)).perform(click())
        onView(withId(R.id.select_theme)).perform(click())
        onView(withContentDescription(R.string.theme_paper)).perform(click())
        assertEquals(ColorTheme.PAPER, preferences.theme)
        onView(withId(R.id.difficulty_name)).check(matches(withText(R.string.difficulty_medium)))
        scenario.recreate()
        assertEquals(ColorTheme.PAPER, preferences.theme)
    }

    @Test
    fun difficultiesWrapInBothDirections() {
        repeat(2) { onView(withId(R.id.previous_difficulty)).perform(click()) }
        onView(withId(R.id.difficulty_name)).check(matches(withText(R.string.difficulty_extreme)))
        onView(withId(R.id.next_difficulty)).perform(click())
        onView(withId(R.id.difficulty_name)).check(matches(withText(R.string.difficulty_beginner)))
    }

    @Test
    fun settingsChangesTheme() {
        onView(withId(R.id.settings)).perform(click())
        onView(withId(R.id.settings_theme)).perform(click())
        onView(withContentDescription(R.string.theme_ocean)).perform(click())
        pressBack()
        assertEquals(ColorTheme.OCEAN, preferences.theme)
    }

    @Test
    fun actionsShowHonestEmptyStates() {
        onView(withId(R.id.best_times)).perform(click())
        onView(withText(R.string.no_best_times)).check(matches(withText(R.string.no_best_times)))
        pressBack()
        onView(withId(R.id.about)).perform(click())
        onView(withId(R.id.about_close)).check(matches(withText(R.string.close)))
    }

    @Test
    fun levelsSurviveRecreationAndAreIndependentPerDifficulty() {
        repeat(2) { onView(withId(R.id.next_level)).perform(click()) }
        checkLevel(3)
        onView(withId(R.id.next_difficulty)).perform(click())
        checkLevel(1)
        onView(withId(R.id.next_level)).perform(click())
        checkLevel(2)
        onView(withId(R.id.previous_difficulty)).perform(click())
        checkLevel(3)
        scenario.recreate()
        checkLevel(3)
    }

    @Test
    fun theFirstLevelCannotBeDecremented() {
        onView(withId(R.id.previous_level)).check(matches(not(isEnabled())))
        onView(withId(R.id.next_level)).perform(click())
        onView(withId(R.id.previous_level)).perform(click())
        checkLevel(1)
        onView(withId(R.id.previous_level)).check(matches(not(isEnabled())))
    }

    private fun checkLevel(level: Int) {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        onView(withId(R.id.level_name)).check(matches(withText(context.getString(R.string.level_number, level))))
    }

    private fun resetLevels(): Map<Difficulty, Int> = Difficulty.entries.associateWith {
        preferences.difficulty = it
        val previous = preferences.level
        preferences.level = 1
        previous
    }
}
