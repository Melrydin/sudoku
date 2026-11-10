package sudoku.domain.grading.techniques

import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class NakedTripleDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.NAKED_TRIPLE

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        // TODO: Implement Naked Triple detection and return the first applicable step.
        return TechniqueSearchResult.NotImplemented
    }
}
