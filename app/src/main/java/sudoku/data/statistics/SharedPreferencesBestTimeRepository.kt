package sudoku.data.statistics

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import sudoku.domain.common.OptionalValue
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.statistics.BestTimeRepository
import sudoku.domain.statistics.GameResult

class SharedPreferencesBestTimeRepository(context: Context) : BestTimeRepository {
    private val preferences: SharedPreferences = context.getSharedPreferences("best_times", Context.MODE_PRIVATE)

    override fun record(result: GameResult) {
        if (preferences.contains(result.gameId)) return
        preferences.edit(commit = true) { putString(result.gameId, GameResultCodec.encode(result)) }
    }

    override fun find(gameId: String): OptionalValue<GameResult> =
        preferences.getString(gameId, null)?.let(GameResultCodec::decode) ?: OptionalValue.Absent

    override fun forDifficulty(difficulty: Difficulty): List<GameResult> = preferences.all.values
        .flatMap { value: Any? -> if (value is String) GameResultCodec.decode(value).asList() else emptyList() }
        .filter { it.puzzle.difficulty == difficulty }
        .sortedWith(compareBy<GameResult> { it.elapsedMillis }.thenBy { it.assistancePoints }
            .thenBy { it.finishedAt })
}
