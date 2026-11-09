package sudoku.data.preferences

import android.content.Context
import androidx.core.content.edit
import sudoku.domain.settings.BoardLayoutOptions
import sudoku.domain.settings.GameOptions

class GamePreferences(context: Context) {
    private val preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val defaults = GameOptions()
    private val layoutDefaults = BoardLayoutOptions()

    var options: GameOptions
        get() = GameOptions(
            highlightSameDigits = preferences.getBoolean("highlight_same_digits", defaults.highlightSameDigits),
            showRemainingCount = preferences.getBoolean("show_remaining_count", defaults.showRemainingCount),
            prefillNotes = preferences.getBoolean("prefill_notes", defaults.prefillNotes),
            autoRemoveNotes = preferences.getBoolean("auto_remove_notes", defaults.autoRemoveNotes),
            hideCompletedDigits = preferences.getBoolean("hide_completed_digits", defaults.hideCompletedDigits),
            boardAnimations = preferences.getBoolean("board_animations", defaults.boardAnimations),
            showTimer = preferences.getBoolean("show_timer", defaults.showTimer),
            circleGivens = preferences.getBoolean("circle_givens", defaults.circleGivens),
            doubleTapErase = preferences.getBoolean("double_tap_erase", defaults.doubleTapErase),
            holdToErase = preferences.getBoolean("hold_to_erase", defaults.holdToErase),
        )
        set(value) = preferences.edit {
            putBoolean("highlight_same_digits", value.highlightSameDigits)
            putBoolean("show_remaining_count", value.showRemainingCount)
            putBoolean("prefill_notes", value.prefillNotes)
            putBoolean("auto_remove_notes", value.autoRemoveNotes)
            putBoolean("hide_completed_digits", value.hideCompletedDigits)
            putBoolean("board_animations", value.boardAnimations)
            putBoolean("show_timer", value.showTimer)
            putBoolean("circle_givens", value.circleGivens)
            putBoolean("double_tap_erase", value.doubleTapErase)
            putBoolean("hold_to_erase", value.holdToErase)
        }

    var boardLayout: BoardLayoutOptions
        get() = BoardLayoutOptions(
            preferences.getFloat("board_width", layoutDefaults.widthFraction).coerceIn(0.6f, 1f),
            preferences.getFloat("board_vertical", layoutDefaults.verticalBias).coerceIn(0f, 1f),
            preferences.getFloat("board_horizontal", layoutDefaults.horizontalBias).coerceIn(0f, 1f),
        )
        set(value) = preferences.edit {
            putFloat("board_width", value.widthFraction)
            putFloat("board_vertical", value.verticalBias)
            putFloat("board_horizontal", value.horizontalBias)
        }
}
