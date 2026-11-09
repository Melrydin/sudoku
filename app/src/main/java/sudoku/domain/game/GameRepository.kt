package sudoku.domain.game

import sudoku.domain.puzzle.PuzzleId

interface GameRepository {
    fun load(puzzle: PuzzleId): GameSession?
    fun save(puzzle: PuzzleId, session: GameSession)
    fun delete(puzzle: PuzzleId)
}
