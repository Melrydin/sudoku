package sudoku.domain.grading.techniques

import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class LockedCandidatesPointingDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.LOCKED_CANDIDATES_POINTING

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        // TODO: Implement Locked Candidates Pointing detection and return the first applicable step.
        return TechniqueSearchResult.NotImplemented
    }
}
