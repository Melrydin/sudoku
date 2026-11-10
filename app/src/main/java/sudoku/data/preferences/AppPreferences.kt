package sudoku.data.preferences

import android.content.Context
import androidx.core.content.edit
import sudoku.domain.generation.GeneratorConfig
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.PuzzleId
import sudoku.domain.settings.ColorTheme

class AppPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("home", Context.MODE_PRIVATE)

    var theme: ColorTheme
        get() = ColorTheme.entries.find { it.name == preferences.getString("theme", null) } ?: ColorTheme.AURUM
        set(value) = preferences.edit { putString("theme", value.name) }

    var difficulty: Difficulty
        get() = Difficulty.entries.find { it.name == preferences.getString("difficulty", null) } ?: Difficulty.EASY
        set(value) = preferences.edit { putString("difficulty", value.name) }

    var level: Int
        get() = preferences.getInt("level_${difficulty.id}", 1).coerceAtLeast(1)
        set(value) {
            require(value >= 1)
            preferences.edit { putInt("level_${difficulty.id}", value) }
        }

    val puzzleId: PuzzleId get() = PuzzleId(difficulty, level, GeneratorConfig.VERSION)
}
