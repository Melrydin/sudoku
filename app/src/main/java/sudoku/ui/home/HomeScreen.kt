package sudoku.ui.home

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.core.view.isInvisible
import sudoku.R
import sudoku.data.preferences.AppPreferences
import sudoku.domain.puzzle.PuzzleId
import sudoku.domain.settings.ColorTheme
import sudoku.ui.about.AboutDialog
import sudoku.ui.settings.SettingsActivity
import sudoku.ui.statistics.TimesActivity
import sudoku.ui.common.label
import sudoku.ui.theme.ThemePalette
import sudoku.ui.theme.label

class HomeScreen(
    private val root: View,
    private val preferences: AppPreferences,
    private val onThemeSelected: (ColorTheme) -> Unit,
    private val onNewGame: (PuzzleId) -> Unit,
    private val onContinue: (PuzzleId) -> Unit,
    private val hasSavedGame: (PuzzleId) -> Boolean,
) {
    private val about = AboutDialog(root.context)

    fun bind() {
        bindThemeSelector()
        bindDifficultySelector()
        bindLevelSelector()
        bindActions()
        renderSelection()
    }

    private fun bindThemeSelector() {
        root.findViewById<View>(R.id.select_theme).apply {
            contentDescription = root.context.getString(
                R.string.theme_description, root.context.getString(preferences.theme.label),
            )
            setOnClickListener { ThemePalette(root.context, preferences.theme, onThemeSelected).show(it) }
        }
    }

    private fun bindDifficultySelector() {
        root.findViewById<View>(R.id.previous_difficulty).setOnClickListener { shiftDifficulty(-1) }
        root.findViewById<View>(R.id.next_difficulty).setOnClickListener { shiftDifficulty(1) }
    }

    private fun bindLevelSelector() {
        root.findViewById<View>(R.id.previous_level).setOnClickListener { shiftLevel(-1) }
        root.findViewById<View>(R.id.next_level).setOnClickListener { shiftLevel(1) }
    }

    private fun bindActions() {
        root.findViewById<View>(R.id.new_game).setOnClickListener { onNewGame(preferences.puzzleId) }
        root.findViewById<View>(R.id.continue_game).setOnClickListener { onContinue(preferences.puzzleId) }
        root.findViewById<View>(R.id.settings).setOnClickListener {
            root.context.startActivity(Intent(root.context, SettingsActivity::class.java))
        }
        root.findViewById<View>(R.id.best_times).setOnClickListener {
            root.context.startActivity(Intent(root.context, TimesActivity::class.java))
        }
        root.findViewById<View>(R.id.about).setOnClickListener { about.show() }
    }

    private fun shiftDifficulty(step: Int) {
        preferences.difficulty = preferences.difficulty.shiftedBy(step)
        renderSelection()
    }

    private fun shiftLevel(step: Int) {
        val next = preferences.level.toLong() + step
        if (next !in 1L..Int.MAX_VALUE.toLong()) return
        preferences.level = next.toInt()
        renderSelection()
    }

    private fun renderSelection() {
        val label = root.findViewById<TextView>(R.id.difficulty_name)
        label.setText(preferences.difficulty.label)
        label.contentDescription = root.context.getString(R.string.difficulty_description, label.text)
        root.findViewById<TextView>(R.id.level_name).text =
            root.context.getString(R.string.level_number, preferences.level)
        root.findViewById<View>(R.id.previous_level).isEnabled = preferences.level > 1
        root.findViewById<View>(R.id.next_level).isEnabled = preferences.level < Int.MAX_VALUE
        root.findViewById<View>(R.id.continue_game).isInvisible = !hasSavedGame(preferences.puzzleId)
    }
}
