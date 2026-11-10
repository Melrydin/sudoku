package sudoku.domain.grading.techniques

import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class HiddenPairDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.HIDDEN_PAIR

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        // TODO: Implement Hidden Pair detection and return the first applicable step.
        return TechniqueSearchResult.NotImplemented
    }
}
