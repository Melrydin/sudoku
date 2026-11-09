package sudoku

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import sudoku.home.Difficulty
import sudoku.home.HomePreferences
import sudoku.home.HomeTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    private val preferences = HomePreferences(ApplicationProvider.getApplicationContext())
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var previousTheme: HomeTheme
    private lateinit var previousDifficulty: Difficulty

    @Before
    fun setUp() {
        previousTheme = preferences.theme
        previousDifficulty = preferences.difficulty
        preferences.theme = HomeTheme.AURUM
        preferences.difficulty = Difficulty.EASY
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
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
        assertEquals(HomeTheme.PAPER, preferences.theme)
        onView(withId(R.id.difficulty_name)).check(matches(withText(R.string.difficulty_medium)))
        scenario.recreate()
        assertEquals(HomeTheme.PAPER, preferences.theme)
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
        assertEquals(HomeTheme.OCEAN, preferences.theme)
    }

    @Test
    fun actionsShowHonestEmptyStates() {
        onView(withId(R.id.best_times)).perform(click())
        onView(withText(R.string.no_best_times)).check(matches(withText(R.string.no_best_times)))
        pressBack()
        onView(withId(R.id.about)).perform(click())
        onView(withId(R.id.about_close)).check(matches(withText(R.string.close)))
    }
}
