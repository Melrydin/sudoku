package sudoku.domain.grading.techniques

import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.LogicalStep
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class HiddenSingleDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.HIDDEN_SINGLE

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        val board = SudokuUnits.all
        board.forEach { unit ->
            for (digit in 1..9) {
                val matchingCells = unit.filter { cell ->
                    digit in state.candidates[cell]
                }
                val cell = matchingCells.singleOrNull()
                if (cell != null) {
                    return TechniqueSearchResult.Found(
                        LogicalStep(
                            technique,
                            mapOf(cell to digit),
                        )
                    )
                }
            }
        }
        return TechniqueSearchResult.NotFound
    }
}
