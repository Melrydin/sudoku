package sudoku.home

import android.content.Context
import androidx.core.content.edit

class HomePreferences(context: Context) {
    private val preferences = context.getSharedPreferences("home", Context.MODE_PRIVATE)

    var theme: HomeTheme
        get() = HomeTheme.entries.find { it.name == preferences.getString("theme", null) } ?: HomeTheme.AURUM
        set(value) = preferences.edit { putString("theme", value.name) }

    var difficulty: Difficulty
        get() = Difficulty.entries.find { it.name == preferences.getString("difficulty", null) } ?: Difficulty.EASY
        set(value) = preferences.edit { putString("difficulty", value.name) }
}
