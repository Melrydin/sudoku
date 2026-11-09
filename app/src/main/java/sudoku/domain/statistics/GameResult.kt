package sudoku.domain.statistics

import sudoku.domain.game.GameAssist
import sudoku.domain.puzzle.PuzzleId

data class GameResult(
    val gameId: String,
    val puzzle: PuzzleId,
    val elapsedMillis: Long,
    val assistancePoints: Int,
    val finishedAt: Long,
) {
    init {
        require(gameId.isNotBlank())
        require(elapsedMillis >= 0 && finishedAt >= 0)
        require(assistancePoints in 0..GameAssist.entries.size)
    }
}
