package sudoku.domain.grading.techniques

import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.LogicalStep
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class NakedSingleDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.NAKED_SINGLE

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        val board = state.board.cells.indices

        board.forEach { cell ->
            val candidates = state.candidates[cell]
            if (candidates.size == 1){
                val candidate = candidates.single()
                return TechniqueSearchResult.Found(
                    LogicalStep(
                    technique,
                    mapOf(cell to candidate)
                )
                )
            }

        }
        return TechniqueSearchResult.NotFound
    }
}
