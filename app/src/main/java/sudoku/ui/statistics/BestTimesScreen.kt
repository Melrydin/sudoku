package sudoku.ui.statistics

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import sudoku.R
import sudoku.domain.game.GameAssist
import sudoku.domain.statistics.GameResult
import sudoku.ui.common.PlayTimeFormatter

class BestTimesScreen(private val root: View) {
    private val entries = root.findViewById<LinearLayout>(R.id.times_entries)

    fun render(results: List<GameResult>) {
        root.findViewById<View>(R.id.times_empty).isVisible = results.isEmpty()
        entries.removeAllViews()
        results.forEachIndexed { index, result ->
            val row = LayoutInflater.from(root.context).inflate(R.layout.item_best_time, entries, false)
            bindRow(row, index + 1, result)
            entries.addView(row)
        }
    }

    private fun bindRow(row: View, rank: Int, result: GameResult) {
        row.findViewById<TextView>(R.id.result_rank).text = root.context.getString(R.string.result_rank, rank)
        row.findViewById<TextView>(R.id.result_level).text =
            root.context.getString(R.string.level_number, result.puzzle.level)
        row.findViewById<TextView>(R.id.result_time).text = PlayTimeFormatter.format(result.elapsedMillis)
        row.findViewById<TextView>(R.id.result_assistance).text = root.context.getString(
            R.string.assistance_points, result.assistancePoints, GameAssist.entries.size,
        )
    }
}
