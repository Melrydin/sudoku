package sudoku.domain.generation

import kotlin.random.Random
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.GeneratorVersion
import sudoku.domain.puzzle.PuzzleId

object GeneratorConfig {
    const val APP_SEED: Int = 123456789
    private const val VERSION_ONE_TARGET_CLUES: Int = 36
    const val MAX_CLUE_REMOVAL_ATTEMPTS: Int = 100
    val VERSION: GeneratorVersion = GeneratorVersion.V2

    fun targetClues(puzzle: PuzzleId, random: Random): Int = when (puzzle.version) {
        GeneratorVersion.V1 -> VERSION_ONE_TARGET_CLUES
        GeneratorVersion.V2 -> clueRange(puzzle.difficulty).random(random)
    }

    // Generation targets only; clue counts do not determine technique-based difficulty.
    fun clueRange(difficulty: Difficulty): IntRange = when (difficulty) {
        Difficulty.BEGINNER -> 40..45
        Difficulty.EASY -> 36..39
        Difficulty.MEDIUM -> 32..35
        Difficulty.HARD -> 28..31
        Difficulty.EXTREME -> 24..27
    }
}
