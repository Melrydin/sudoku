package sudoku.ui.statistics

import android.os.Bundle
import android.view.View
import android.widget.TextView
import sudoku.R
import sudoku.app.appContainer
import sudoku.ui.common.label
import sudoku.domain.puzzle.Difficulty
import sudoku.ui.theme.ThemePalette
import sudoku.ui.common.ThemedActivity

class TimesActivity : ThemedActivity() {
    private lateinit var screen: BestTimesScreen
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_times)
        applySystemInsets(findViewById(R.id.times_root))
        screen = BestTimesScreen(findViewById(R.id.times_root))
        findViewById<View>(R.id.times_back).setOnClickListener { finish() }
        findViewById<View>(R.id.times_theme).setOnClickListener {
            ThemePalette(this, preferences.theme, ::selectTheme).show(it)
        }
        findViewById<View>(R.id.times_previous).setOnClickListener { shiftDifficulty(-1) }
        findViewById<View>(R.id.times_next).setOnClickListener { shiftDifficulty(1) }
        renderDifficulty()
    }

    override fun onResume() {
        super.onResume()
        if (!refreshTheme()) renderDifficulty()
    }

    private fun shiftDifficulty(step: Int) {
        preferences.difficulty = preferences.difficulty.shiftedBy(step)
        renderDifficulty()
    }

    private fun renderDifficulty() {
        findViewById<TextView>(R.id.times_difficulty).setText(preferences.difficulty.label)
        screen.render(appContainer.bestTimes.forDifficulty(preferences.difficulty))
    }
}
