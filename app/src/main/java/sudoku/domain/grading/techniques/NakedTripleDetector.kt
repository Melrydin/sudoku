package sudoku.domain.grading.techniques

import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.LogicalStep
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult
import sudoku.domain.grading.techniques.helpers.combinationsOfThree
import sudoku.domain.grading.techniques.helpers.findNakedSubsetEliminations

class NakedTripleDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.NAKED_TRIPLE

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        val units: List<List<Int>> = SudokuUnits.all

        units.forEach { unit: List<Int> ->
            val candidateCells: List<Int> =
                unit.filter { cell: Int -> state.candidates[cell].size in 2..3 }
            val combinations: List<List<Int>> = combinationsOfThree(candidateCells)
            for (combination: List<Int> in combinations) {
                if (!isNakedTriple(combination, state)) continue
                val eliminations: Map<Int, Set<Int>> = findNakedSubsetEliminations(unit, combination, state)
                if (eliminations.isEmpty()) continue
                return TechniqueSearchResult.Found(
                    LogicalStep(technique, eliminations = eliminations)
                )
            }
        }
        return TechniqueSearchResult.NotFound
    }

    private fun isNakedTriple(
        cells: List<Int>,
        state: LogicalState,
    ): Boolean {
        if (cells.size != 3 || cells.toSet().size != 3) return false

        val candidateSets: List<Set<Int>> = cells.map { cell: Int ->
            state.candidates[cell]
        }
        if (candidateSets.any { candidates: Set<Int> -> candidates.size !in 2..3 }) return false

        val combinedCandidates: Set<Int> = candidateSets.flatten().toSet()
        return combinedCandidates.size == 3
    }
}
