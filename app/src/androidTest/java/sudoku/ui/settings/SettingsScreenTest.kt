package sudoku.ui.settings

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sudoku.R
import sudoku.data.preferences.GamePreferences
import sudoku.domain.settings.GameOptions
import sudoku.testing.ScreenCapture

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    private val preferences = GamePreferences(ApplicationProvider.getApplicationContext())
    private lateinit var previousOptions: GameOptions
    private lateinit var scenario: ActivityScenario<SettingsActivity>

    @Before
    fun setUp() {
        previousOptions = preferences.options
        preferences.options = GameOptions()
        scenario = ActivityScenario.launch(SettingsActivity::class.java)
        onView(withId(R.id.settings_assists)).perform(scrollTo(), click())
    }

    @After
    fun tearDown() {
        scenario.close()
        preferences.options = previousOptions
    }

    @Test
    fun individualSettingsSurviveRecreationWithoutChangingOtherSwitches() {
        listOf(R.id.setting_highlight, R.id.setting_prefill, R.id.setting_hide_digits).forEach(::toggle)
        scenario.recreate()
        checkToggle(R.id.setting_highlight, false)
        checkToggle(R.id.setting_remaining, true)
        checkToggle(R.id.setting_prefill, true)
        checkToggle(R.id.setting_remove_notes, true)
        checkToggle(R.id.setting_hide_digits, false)
        val restored = GamePreferences(ApplicationProvider.getApplicationContext()).options
        val expected = GameOptions(highlightSameDigits = false, prefillNotes = true, hideCompletedDigits = false)
        assertEquals(expected, restored)
        reopenSection(R.id.settings_assists)
        ScreenCapture.capture("settings-assists.png")
    }

    @Test
    fun everyOptionCanBeDisabledAndEnabledIndependently() {
        val rows = listOf(R.id.setting_highlight, R.id.setting_remaining, R.id.setting_prefill,
            R.id.setting_remove_notes, R.id.setting_hide_digits)
        rows.forEach(::toggle)
        assertEquals(GameOptions(false, false, true, false, false), preferences.options)
        rows.forEach(::toggle)
        assertEquals(GameOptions(), preferences.options)
    }

    @Test
    fun gameplayOptionsAreIndependentAndSaved() {
        pressBack()
        ScreenCapture.capture("settings-menu.png")
        onView(withId(R.id.settings_gameplay)).perform(scrollTo(), click())
        listOf(R.id.setting_animations, R.id.setting_timer, R.id.setting_circles,
            R.id.setting_double_tap, R.id.setting_hold).forEach(::toggle)
        scenario.recreate()
        val expected = GameOptions(boardAnimations = false, showTimer = false, circleGivens = true,
            doubleTapErase = true, holdToErase = true)
        assertEquals(expected, preferences.options)
        checkToggle(R.id.setting_timer, false)
        checkToggle(R.id.setting_circles, true)
        checkToggle(R.id.setting_hold, true)
        reopenSection(R.id.settings_gameplay)
        ScreenCapture.capture("settings-gameplay.png")
    }

    private fun toggle(id: Int) {
        onView(allOf(withId(R.id.setting_toggle), isDescendantOfA(withId(id)))).perform(scrollTo(), click())
    }

    private fun reopenSection(id: Int) {
        pressBack()
        onView(withId(id)).perform(scrollTo(), click())
    }

    private fun checkToggle(id: Int, checked: Boolean) {
        onView(allOf(withId(R.id.setting_toggle), isDescendantOfA(withId(id))))
            .check(matches(if (checked) isChecked() else not(isChecked())))
    }
}
