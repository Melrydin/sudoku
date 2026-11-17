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
        val board: List<List<Int>> = SudokuUnits.all
        board.forEach { unit: List<Int> ->
            for (digit: Int in 1..9) {
                val matchingCells: List<Int> = unit.filter { cell: Int ->
                    digit in state.candidates[cell]
                }
                val cell: Int = matchingCells.singleOrNull() ?: -1
                if (cell >= 0) {
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
