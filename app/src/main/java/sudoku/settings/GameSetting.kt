package sudoku.settings

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import sudoku.R
import sudoku.game.GameOptions

enum class GameSetting(
    val section: SettingsSection,
    @param:IdRes val id: Int,
    @param:StringRes val title: Int,
    @param:StringRes val summary: Int,
    val read: (GameOptions) -> Boolean,
    val update: (GameOptions, Boolean) -> GameOptions,
) {
    HIGHLIGHT(SettingsSection.ASSISTS, R.id.setting_highlight, R.string.setting_highlight, 0,
        { it.highlightSameDigits }, { options, enabled -> options.copy(highlightSameDigits = enabled) }),
    REMAINING(SettingsSection.ASSISTS, R.id.setting_remaining, R.string.setting_remaining, 0,
        { it.showRemainingCount }, { options, enabled -> options.copy(showRemainingCount = enabled) }),
    REMOVE_NOTES(SettingsSection.ASSISTS, R.id.setting_remove_notes, R.string.setting_remove_notes, 0,
        { it.autoRemoveNotes }, { options, enabled -> options.copy(autoRemoveNotes = enabled) }),
    PREFILL(SettingsSection.ASSISTS, R.id.setting_prefill, R.string.setting_prefill, R.string.setting_prefill_summary,
        { it.prefillNotes }, { options, enabled -> options.copy(prefillNotes = enabled) }),
    HIDE_DIGITS(SettingsSection.ASSISTS, R.id.setting_hide_digits, R.string.setting_hide_digits, 0,
        { it.hideCompletedDigits }, { options, enabled -> options.copy(hideCompletedDigits = enabled) }),
    ANIMATIONS(SettingsSection.GAMEPLAY, R.id.setting_animations, R.string.setting_animations, 0,
        { it.boardAnimations }, { options, enabled -> options.copy(boardAnimations = enabled) }),
    TIMER(SettingsSection.GAMEPLAY, R.id.setting_timer, R.string.setting_timer, R.string.setting_timer_summary,
        { it.showTimer }, { options, enabled -> options.copy(showTimer = enabled) }),
    CIRCLES(SettingsSection.GAMEPLAY, R.id.setting_circles, R.string.setting_circles, R.string.setting_circles_summary,
        { it.circleGivens }, { options, enabled -> options.copy(circleGivens = enabled) }),
    DOUBLE_TAP(SettingsSection.GAMEPLAY, R.id.setting_double_tap, R.string.setting_double_tap, 0,
        { it.doubleTapErase }, { options, enabled -> options.copy(doubleTapErase = enabled) }),
    HOLD(SettingsSection.GAMEPLAY, R.id.setting_hold, R.string.setting_hold, 0,
        { it.holdToErase }, { options, enabled -> options.copy(holdToErase = enabled) }),
}
