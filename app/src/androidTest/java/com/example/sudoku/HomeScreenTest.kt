package com.example.sudoku

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sudoku.home.Difficulty
import com.example.sudoku.home.HomePreferences
import com.example.sudoku.home.HomeTheme
import org.junit.After
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
    }

    @Test
    fun themeSelectionPreservesDifficulty() {
        onView(withId(R.id.next_difficulty)).perform(click())
        onView(withId(R.id.next_theme)).perform(click())
        onView(withId(R.id.theme_name)).check(matches(withText(R.string.theme_paper)))
        onView(withId(R.id.difficulty_name)).check(matches(withText(R.string.difficulty_medium)))
        scenario.recreate()
        onView(withId(R.id.theme_name)).check(matches(withText(R.string.theme_paper)))
    }

    @Test
    fun selectorsWrapInBothDirections() {
        onView(withId(R.id.previous_theme)).perform(click())
        onView(withId(R.id.theme_name)).check(matches(withText(R.string.theme_midnight)))
        onView(withId(R.id.next_theme)).perform(click())
        onView(withId(R.id.theme_name)).check(matches(withText(R.string.theme_aurum)))
        repeat(2) { onView(withId(R.id.previous_difficulty)).perform(click()) }
        onView(withId(R.id.difficulty_name)).check(matches(withText(R.string.difficulty_extreme)))
        onView(withId(R.id.next_difficulty)).perform(click())
        onView(withId(R.id.difficulty_name)).check(matches(withText(R.string.difficulty_beginner)))
    }

    @Test
    fun settingsChangesTheme() {
        onView(withId(R.id.settings)).perform(click())
        onView(withText(R.string.theme_ocean)).perform(click())
        onView(withId(R.id.theme_name)).check(matches(withText(R.string.theme_ocean)))
    }

    @Test
    fun actionsShowHonestEmptyStates() {
        onView(withId(R.id.best_times)).perform(click())
        onView(withText(R.string.no_best_times)).check(matches(withText(R.string.no_best_times)))
        onView(withText(R.string.close)).perform(click())
        onView(withId(R.id.about)).perform(click())
        onView(withText(R.string.about_message)).check(matches(withText(R.string.about_message)))
    }
}
