package sudoku.ui.game.result

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import sudoku.R
import sudoku.app.appContainer
import sudoku.domain.game.GameAssist
import sudoku.domain.statistics.GameResult
import sudoku.ui.common.PlayTimeFormatter
import sudoku.ui.common.ThemedActivity
import sudoku.ui.common.label
import sudoku.ui.navigation.HomeNavigation
import sudoku.ui.statistics.TimesActivity

class VictoryActivity : ThemedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra(EXTRA_GAME_ID).orEmpty()
        val result = appContainer.bestTimes.find(id)
        if (result == null) { finish(); return }
        setContentView(R.layout.activity_victory)
        applySystemInsets(findViewById(R.id.victory_root))
        render(result)
        bindNavigation(result)
    }

    override fun onResume() {
        super.onResume()
        refreshTheme()
    }

    private fun render(result: GameResult) {
        findViewById<TextView>(R.id.victory_puzzle).text = getString(
            R.string.game_identity, getString(result.puzzle.difficulty.label), result.puzzle.level,
        )
        findViewById<TextView>(R.id.victory_time).text = PlayTimeFormatter.format(result.elapsedMillis)
        findViewById<TextView>(R.id.victory_assistance).text = getString(
            R.string.assistance_points, result.assistancePoints, GameAssist.entries.size,
        )
    }

    private fun bindNavigation(result: GameResult) {
        findViewById<View>(R.id.victory_home).setOnClickListener { HomeNavigation.open(this, result.puzzle) }
        findViewById<View>(R.id.victory_best_times).setOnClickListener {
            preferences.difficulty = result.puzzle.difficulty
            startActivity(Intent(this, TimesActivity::class.java))
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = HomeNavigation.open(this@VictoryActivity, result.puzzle)
        })
    }

    companion object {
        private const val EXTRA_GAME_ID = "game_id"

        fun intent(context: Context, gameId: String): Intent =
            Intent(context, VictoryActivity::class.java).putExtra(EXTRA_GAME_ID, gameId)
    }
}
