package sudoku.ui.common

import androidx.annotation.StringRes
import sudoku.R
import sudoku.domain.puzzle.Difficulty

val Difficulty.label: Int
    @StringRes get() = when (this) {
        Difficulty.BEGINNER -> R.string.difficulty_beginner
        Difficulty.EASY -> R.string.difficulty_easy
        Difficulty.MEDIUM -> R.string.difficulty_medium
        Difficulty.HARD -> R.string.difficulty_hard
        Difficulty.EXTREME -> R.string.difficulty_extreme
    }
