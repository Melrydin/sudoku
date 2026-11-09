package sudoku.home

import android.os.Bundle
import android.view.View
import android.widget.TextView
import sudoku.R
import sudoku.ui.ThemedActivity

class TimesActivity : ThemedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_times)
        applySystemInsets(findViewById(R.id.times_root))
        findViewById<View>(R.id.times_back).setOnClickListener { finish() }
        findViewById<View>(R.id.times_theme).setOnClickListener {
            HomeDialogs(this).showThemes(it, preferences.theme, ::selectTheme)
        }
        findViewById<View>(R.id.times_previous).setOnClickListener { shiftDifficulty(-1) }
        findViewById<View>(R.id.times_next).setOnClickListener { shiftDifficulty(1) }
        renderDifficulty()
    }

    private fun shiftDifficulty(step: Int) {
        preferences.difficulty = preferences.difficulty.shiftedBy(step)
        renderDifficulty()
    }

    private fun renderDifficulty() {
        findViewById<TextView>(R.id.times_difficulty).setText(preferences.difficulty.label)
    }
}
