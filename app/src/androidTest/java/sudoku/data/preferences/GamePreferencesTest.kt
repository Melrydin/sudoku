package sudoku.data.preferences

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sudoku.domain.settings.BoardLayoutOptions
import sudoku.domain.settings.GameOptions

@RunWith(AndroidJUnit4::class)
class GamePreferencesTest {
    private val base: Context = ApplicationProvider.getApplicationContext()
    private val isolated = object : ContextWrapper(base) {
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
            base.getSharedPreferences("sudoku_defaults_test_$name", mode)
    }

    @Before
    fun setUp() {
        base.deleteSharedPreferences("sudoku_defaults_test_settings")
    }

    @After
    fun tearDown() {
        base.deleteSharedPreferences("sudoku_defaults_test_settings")
    }

    @Test
    fun freshSettingsUseTheDefaultsCapturedFromThePixel() {
        val preferences = GamePreferences(isolated)
        assertFalse(preferences.options.circleGivens)
        assertTrue(preferences.options.highlightSameDigits)
        assertTrue(preferences.options.showRemainingCount)
        assertTrue(preferences.options.showTimer)
        assertEquals(BoardLayoutOptions(1f, 0.5f, 0.5f), preferences.boardLayout)
    }

    @Test
    fun savedSettingsOverrideTheNewDefaults() {
        GamePreferences(isolated).options = GameOptions(circleGivens = true)
        assertTrue(GamePreferences(isolated).options.circleGivens)
    }
}
