package sudoku.domain.generation

import sudoku.domain.puzzle.GeneratorVersion

object GeneratorConfig {
    const val APP_SEED: Int = 123456789
    const val TARGET_CLUES: Int = 36
    val VERSION: GeneratorVersion = GeneratorVersion.V1
}
