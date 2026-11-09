package sudoku.game

import sudoku.generator.ClueRemovalResult
import sudoku.generator.ClueRemover
import sudoku.generator.GeneratorConfig
import sudoku.generator.PuzzleGenerator
import sudoku.generator.PuzzleRandom
import sudoku.generator.SudokuGenerator

object NewGameFactory {
    fun create(puzzle: PuzzleId, prefillNotes: Boolean): GameSession {
        val random = PuzzleRandom.forPuzzle(puzzle)
        val generator = PuzzleGenerator({ SudokuGenerator(random).generate() }, ClueRemover(random = random))
        val result = generator.generate(GeneratorConfig.TARGET_CLUES)
        check(result is ClueRemovalResult.Removed) { "Puzzle generation requires a solution counter." }
        return fromPuzzle(result.puzzle, prefillNotes)
    }

    fun fromPuzzle(puzzle: SudokuBoard, prefillNotes: Boolean): GameSession {
        require(!SudokuRules.isComplete(puzzle)) { "A new game needs editable cells." }
        val state = GameState(puzzle)
        val notes = if (prefillNotes) GameNotes.prefill(puzzle) else state.notes
        return GameSession(state.copy(notes = notes))
    }
}
