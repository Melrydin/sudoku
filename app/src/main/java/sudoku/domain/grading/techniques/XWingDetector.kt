package sudoku.domain.grading.techniques

import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class XWingDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.X_WING

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        // TODO: Implement X-Wing detection and return the first applicable step.
        return TechniqueSearchResult.NotImplemented
    }
}
