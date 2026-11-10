package sudoku.domain.game

import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuRules
import sudoku.domain.generation.ClueRemovalResult
import sudoku.domain.generation.ClueRemover
import sudoku.domain.generation.GeneratorConfig
import sudoku.domain.generation.PuzzleGenerator
import sudoku.domain.generation.PuzzleRandom
import sudoku.domain.generation.SudokuGenerator
import sudoku.domain.puzzle.GeneratorVersion
import sudoku.domain.puzzle.PuzzleId

object NewGameFactory {
    fun create(puzzle: PuzzleId, prefillNotes: Boolean): GameSession {
        val random = PuzzleRandom.forPuzzle(puzzle)
        val solution = SudokuGenerator(random).generate()
        val generator = PuzzleGenerator({ solution }, ClueRemover(random = random))
        val targetClues = GeneratorConfig.targetClues(puzzle, random)
        val attempts = if (puzzle.version == GeneratorVersion.V1) 1 else GeneratorConfig.MAX_CLUE_REMOVAL_ATTEMPTS
        val result = generator.generate(targetClues, attempts)
        check(result is ClueRemovalResult.Removed) { "Puzzle generation requires a solution counter." }
        check(result.targetReached || puzzle.version == GeneratorVersion.V1) { "The clue target could not be reached." }
        return fromPuzzle(result.puzzle, prefillNotes, solution)
    }

    fun fromPuzzle(puzzle: SudokuBoard, prefillNotes: Boolean, solution: SudokuBoard? = null): GameSession {
        require(!SudokuRules.isComplete(puzzle)) { "A new game needs editable cells." }
        val state = GameState(puzzle)
        val notes = if (prefillNotes) GameNotes.prefill(puzzle) else state.notes
        val assistance = if (prefillNotes) setOf(GameAssist.PREFILL_NOTES) else emptySet()
        return GameSession(state.copy(notes = notes), initialSolution = solution, initialAssistance = assistance)
    }
}
