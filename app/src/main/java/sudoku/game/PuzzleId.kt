package sudoku.game

import sudoku.generator.GeneratorConfig
import sudoku.generator.GeneratorVersion

data class PuzzleId(
    val difficulty: Difficulty,
    val level: Int = 1,
    val version: GeneratorVersion = GeneratorConfig.VERSION,
) {
    init {
        require(level >= 1) { "Level must be at least 1." }
    }

    val key: String get() = "v${version.number}:${difficulty.id}:$level"
}
