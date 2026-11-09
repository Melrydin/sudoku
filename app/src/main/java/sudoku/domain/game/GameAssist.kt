package sudoku.domain.game

import sudoku.domain.settings.GameOptions

enum class GameAssist {
    SAME_DIGITS,
    REMAINING_COUNT,
    PREFILL_NOTES,
    REMOVE_NOTES,
    HIDE_DIGITS;

    companion object {
        fun active(options: GameOptions): Set<GameAssist> = setOfNotNull(
            SAME_DIGITS.takeIf { options.highlightSameDigits },
            REMAINING_COUNT.takeIf { options.showRemainingCount },
            PREFILL_NOTES.takeIf { options.prefillNotes },
            REMOVE_NOTES.takeIf { options.autoRemoveNotes },
            HIDE_DIGITS.takeIf { options.hideCompletedDigits },
        )
    }
}
