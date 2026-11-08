package com.example.sudoku.home

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.sudoku.R

class HomeScreen(
    private val root: View,
    private val preferences: HomePreferences,
    private val onThemeSelected: (HomeTheme) -> Unit,
    private val onNewGame: (Difficulty) -> Unit,
    private val onContinue: (Difficulty) -> Unit,
    private val hasSavedGame: (Difficulty) -> Boolean,
) {
    private val dialogs = HomeDialogs(root.context)

    fun bind() {
        bindThemeSelector()
        bindDifficultySelector()
        bindActions()
    }

    private fun bindThemeSelector() {
        val label = root.findViewById<TextView>(R.id.theme_name)
        label.setText(preferences.theme.label)
        label.contentDescription = root.context.getString(R.string.theme_description, label.text)
        root.findViewById<View>(R.id.previous_theme).setOnClickListener { shiftTheme(-1) }
        root.findViewById<View>(R.id.next_theme).setOnClickListener { shiftTheme(1) }
    }

    private fun bindDifficultySelector() {
        renderDifficulty()
        root.findViewById<View>(R.id.previous_difficulty).setOnClickListener { shiftDifficulty(-1) }
        root.findViewById<View>(R.id.next_difficulty).setOnClickListener { shiftDifficulty(1) }
    }

    private fun bindActions() {
        root.findViewById<View>(R.id.new_game).setOnClickListener { onNewGame(preferences.difficulty) }
        root.findViewById<View>(R.id.continue_game).setOnClickListener { onContinue(preferences.difficulty) }
        root.findViewById<View>(R.id.settings).setOnClickListener {
            dialogs.showThemes(preferences.theme, onThemeSelected)
        }
        root.findViewById<View>(R.id.best_times).setOnClickListener { dialogs.showBestTimes() }
        root.findViewById<View>(R.id.about).setOnClickListener { dialogs.showAbout() }
    }

    private fun shiftTheme(step: Int) {
        onThemeSelected(preferences.theme.shiftedBy(step))
    }

    private fun shiftDifficulty(step: Int) {
        preferences.difficulty = preferences.difficulty.shiftedBy(step)
        renderDifficulty()
    }

    private fun renderDifficulty() {
        val label = root.findViewById<TextView>(R.id.difficulty_name)
        label.setText(preferences.difficulty.label)
        label.contentDescription = root.context.getString(R.string.difficulty_description, label.text)
        root.findViewById<View>(R.id.continue_game).isVisible = hasSavedGame(preferences.difficulty)
    }
}
