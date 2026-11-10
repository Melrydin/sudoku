package sudoku.domain.grading.techniques

import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class XYWingDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.XY_WING

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        // TODO: Implement XY-Wing detection and return the first applicable step.
        return TechniqueSearchResult.NotImplemented
    }
}
