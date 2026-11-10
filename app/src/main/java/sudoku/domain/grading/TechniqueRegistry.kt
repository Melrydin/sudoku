package sudoku.domain.grading

import sudoku.domain.grading.techniques.defaultTechniqueDetectors
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.GeneratorVersion

class TechniqueRegistry(detectors: List<TechniqueDetector> = defaultTechniqueDetectors()) {
    private val byTechnique = detectors.associateBy { it.technique }

    init {
        require(byTechnique.size == detectors.size) { "A technique may only have one detector." }
    }

    fun ordered(
        version: GeneratorVersion,
        difficulty: Difficulty = Difficulty.EXTREME,
    ): List<TechniqueDetector> = TechniquePolicy.allowed(version, difficulty).map {
        byTechnique[it.technique] ?: PendingTechnique(it.technique)
    }
}

private class PendingTechnique(override val technique: SolvingTechnique) : TechniqueDetector {
    override fun findStep(state: LogicalState): TechniqueSearchResult = TechniqueSearchResult.NotImplemented
}
