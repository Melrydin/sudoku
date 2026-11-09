package sudoku.home

import androidx.annotation.StringRes
import sudoku.R

enum class Difficulty(@param:StringRes val label: Int) {
    BEGINNER(R.string.difficulty_beginner),
    EASY(R.string.difficulty_easy),
    MEDIUM(R.string.difficulty_medium),
    HARD(R.string.difficulty_hard),
    EXTREME(R.string.difficulty_extreme);

    fun shiftedBy(step: Int): Difficulty = entries[Math.floorMod(ordinal + step, entries.size)]
}
