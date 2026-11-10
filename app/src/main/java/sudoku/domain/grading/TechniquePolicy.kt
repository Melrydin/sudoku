package sudoku.domain.grading

import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.GeneratorVersion

data class TechniqueRule(val technique: SolvingTechnique, val difficulty: Difficulty)

object TechniquePolicy {
    // This list defines V1's search order and app-specific difficulty scale.
    private val versionOne = listOf(
        TechniqueRule(SolvingTechnique.FULL_HOUSE, Difficulty.BEGINNER),
        TechniqueRule(SolvingTechnique.NAKED_SINGLE, Difficulty.BEGINNER),
        TechniqueRule(SolvingTechnique.HIDDEN_SINGLE, Difficulty.EASY),
        TechniqueRule(SolvingTechnique.LOCKED_CANDIDATES_POINTING, Difficulty.MEDIUM),
        TechniqueRule(SolvingTechnique.LOCKED_CANDIDATES_CLAIMING, Difficulty.MEDIUM),
        TechniqueRule(SolvingTechnique.NAKED_PAIR, Difficulty.MEDIUM),
        TechniqueRule(SolvingTechnique.HIDDEN_PAIR, Difficulty.MEDIUM),
        TechniqueRule(SolvingTechnique.NAKED_TRIPLE, Difficulty.HARD),
        TechniqueRule(SolvingTechnique.HIDDEN_TRIPLE, Difficulty.HARD),
        TechniqueRule(SolvingTechnique.X_WING, Difficulty.HARD),
        TechniqueRule(SolvingTechnique.NAKED_QUAD, Difficulty.EXTREME),
        TechniqueRule(SolvingTechnique.HIDDEN_QUAD, Difficulty.EXTREME),
        TechniqueRule(SolvingTechnique.SWORDFISH, Difficulty.EXTREME),
        TechniqueRule(SolvingTechnique.XY_WING, Difficulty.EXTREME),
    )

    fun rules(version: GeneratorVersion): List<TechniqueRule> = when (version) {
        GeneratorVersion.V1, GeneratorVersion.V2 -> versionOne
    }

    fun allowed(version: GeneratorVersion, difficulty: Difficulty): List<TechniqueRule> =
        rules(version).filter { it.difficulty.ordinal <= difficulty.ordinal }
}
