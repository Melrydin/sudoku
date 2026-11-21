package sudoku.domain.grading.techniques

import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.LogicalStep
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult
import sudoku.domain.grading.techniques.helpers.combinationsOfThree
import sudoku.domain.grading.techniques.helpers.findCandidateCells
import sudoku.domain.grading.techniques.helpers.findEliminations

class HiddenTripleDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.HIDDEN_TRIPLE

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        val units: List<List<Int>> = SudokuUnits.all
        val digits: List<Int> = (1..SudokuBoard.SIZE).toList()
        val combinations: List<List<Int>> = combinationsOfThree(digits)
        units.forEach { unit: List<Int> ->
            for (combination: List<Int> in combinations) {
                val candidateCells: Set<Int> = findCandidateCells(unit, combination, state)
                if (candidateCells.size != 3) continue
                val eliminations: Map<Int, Set<Int>> =
                    findEliminations(combination, candidateCells, state)
                if (eliminations.isEmpty()) continue
                return TechniqueSearchResult.Found(
                    LogicalStep(technique, eliminations = eliminations)
                )
            }
        }
        return TechniqueSearchResult.NotFound
    }
}
