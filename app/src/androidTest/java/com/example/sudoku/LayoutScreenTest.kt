package com.example.sudoku

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.RootMatchers.isDialog
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sudoku.game.BoardLayoutOptions
import com.example.sudoku.settings.GamePreferences
import com.example.sudoku.settings.LayoutPreviewActivity
import com.google.android.material.slider.Slider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LayoutScreenTest {
    private val preferences = GamePreferences(ApplicationProvider.getApplicationContext())
    private lateinit var previousLayout: BoardLayoutOptions
    private lateinit var scenario: ActivityScenario<LayoutPreviewActivity>

    @Before
    fun setUp() {
        previousLayout = preferences.boardLayout
        preferences.boardLayout = BoardLayoutOptions()
        scenario = ActivityScenario.launch(LayoutPreviewActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
        preferences.boardLayout = previousLayout
    }

    @Test
    fun previewDoesNotSaveUntilApplied() {
        changeSliders()
        assertEquals(BoardLayoutOptions(), preferences.boardLayout)
        ScreenCapture.capture("layout-preview.png")
        onView(withId(R.id.layout_apply)).perform(click())
        assertEquals(BoardLayoutOptions(0.7f, 0.2f, 0.8f), preferences.boardLayout)
    }

    @Test
    fun cancelKeepsThePreviousLayout() {
        changeSliders()
        onView(withId(R.id.layout_cancel)).perform(click())
        assertEquals(BoardLayoutOptions(), preferences.boardLayout)
    }

    @Test
    fun draftSurvivesRecreationAndHorizontalResetOnlyChangesHorizontalPosition() {
        changeSliders()
        scenario.recreate()
        onView(withId(R.id.layout_reset_horizontal)).inRoot(isDialog()).perform(click())
        onView(withId(R.id.layout_apply)).perform(click())
        assertEquals(BoardLayoutOptions(0.7f, 0.2f, 0.5f), preferences.boardLayout)
    }

    private fun changeSliders() {
        onView(withId(R.id.layout_width)).perform(sliderValue(70f))
        onView(withId(R.id.layout_vertical)).perform(sliderValue(20f))
        onView(withId(R.id.layout_horizontal)).perform(sliderValue(80f))
    }

    private fun sliderValue(value: Float): ViewAction = object : ViewAction {
        override fun getDescription(): String = "Set slider value to $value"
        override fun getConstraints() = isAssignableFrom(Slider::class.java)
        override fun perform(uiController: UiController, view: View) {
            (view as Slider).value = value
            uiController.loopMainThreadUntilIdle()
        }
    }
}
