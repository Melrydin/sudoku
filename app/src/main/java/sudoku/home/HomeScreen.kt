package sudoku.home

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.core.view.isInvisible
import sudoku.R
import sudoku.game.PuzzleId
import sudoku.settings.SettingsActivity

class HomeScreen(
    private val root: View,
    private val preferences: HomePreferences,
    private val onThemeSelected: (HomeTheme) -> Unit,
    private val onNewGame: (PuzzleId) -> Unit,
    private val onContinue: (PuzzleId) -> Unit,
    private val hasSavedGame: (PuzzleId) -> Boolean,
) {
    private val dialogs = HomeDialogs(root.context)

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
            setOnClickListener { dialogs.showThemes(it, preferences.theme, onThemeSelected) }
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
        root.findViewById<View>(R.id.best_times).setOnClickListener { dialogs.showBestTimes() }
        root.findViewById<View>(R.id.about).setOnClickListener { dialogs.showAbout() }
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
