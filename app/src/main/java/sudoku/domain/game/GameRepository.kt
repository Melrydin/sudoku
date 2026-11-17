package sudoku.domain.game

import sudoku.domain.common.OptionalValue
import sudoku.domain.puzzle.PuzzleId

interface GameRepository {
    fun load(puzzle: PuzzleId): OptionalValue<GameSession>
    fun save(puzzle: PuzzleId, session: GameSession)
    fun delete(puzzle: PuzzleId)
}
