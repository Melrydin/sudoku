package sudoku.data.statistics

import android.content.Context
import androidx.core.content.edit
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.statistics.BestTimeRepository
import sudoku.domain.statistics.GameResult

class SharedPreferencesBestTimeRepository(context: Context) : BestTimeRepository {
    private val preferences = context.getSharedPreferences("best_times", Context.MODE_PRIVATE)

    override fun record(result: GameResult) {
        if (preferences.contains(result.gameId)) return
        preferences.edit(commit = true) { putString(result.gameId, GameResultCodec.encode(result)) }
    }

    override fun find(gameId: String): GameResult? =
        preferences.getString(gameId, null)?.let(GameResultCodec::decode)

    override fun forDifficulty(difficulty: Difficulty): List<GameResult> = preferences.all.values
        .mapNotNull { (it as? String)?.let(GameResultCodec::decode) }
        .filter { it.puzzle.difficulty == difficulty }
        .sortedWith(compareBy<GameResult> { it.elapsedMillis }.thenBy { it.assistancePoints }.thenBy { it.finishedAt })
}
