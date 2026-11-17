package sudoku.domain.statistics

import sudoku.domain.common.OptionalValue
import sudoku.domain.puzzle.Difficulty

interface BestTimeRepository {
    fun record(result: GameResult)
    fun find(gameId: String): OptionalValue<GameResult>
    fun forDifficulty(difficulty: Difficulty): List<GameResult>
}
