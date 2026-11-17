package sudoku.domain.game

import kotlin.random.Random
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuRules
import sudoku.domain.common.OptionalValue
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
        val random: Random = PuzzleRandom.forPuzzle(puzzle)
        val solution: SudokuBoard = SudokuGenerator(random).generate()
        val generator: PuzzleGenerator = PuzzleGenerator({ solution }, ClueRemover(random = random))
        val targetClues: Int = GeneratorConfig.targetClues(puzzle, random)
        val attempts: Int =
            if (puzzle.version == GeneratorVersion.V1) 1 else GeneratorConfig.MAX_CLUE_REMOVAL_ATTEMPTS
        val result: ClueRemovalResult = generator.generate(targetClues, attempts)
        check(result is ClueRemovalResult.Removed) { "Puzzle generation requires a solution counter." }
        check(result.targetReached || puzzle.version == GeneratorVersion.V1) { "The clue target could not be reached." }
        return fromPuzzle(result.puzzle, prefillNotes, OptionalValue.Present(solution))
    }

    fun fromPuzzle(
        puzzle: SudokuBoard,
        prefillNotes: Boolean,
        solution: OptionalValue<SudokuBoard> = OptionalValue.Absent
    ): GameSession {
        require(!SudokuRules.isComplete(puzzle)) { "A new game needs editable cells." }
        val state: GameState = GameState(puzzle)
        val notes: List<Set<Int>> = if (prefillNotes) GameNotes.prefill(puzzle) else state.notes
        val assistance: Set<GameAssist> = if (prefillNotes) setOf(GameAssist.PREFILL_NOTES) else emptySet()
        return GameSession(
            state.copy(notes = notes),
            initialSolution = solution,
            initialAssistance = assistance
        )
    }
}
