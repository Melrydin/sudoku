package sudoku.domain.grading.techniques

import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.LogicalStep
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult
import sudoku.domain.grading.techniques.helpers.combinationsOfFour
import sudoku.domain.grading.techniques.helpers.findNakedSubsetEliminations

class NakedQuadDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.NAKED_QUAD

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        val units: List<List<Int>> = SudokuUnits.all

        units.forEach { unit: List<Int> ->
            val candidateCells: List<Int> =
                unit.filter { cell: Int -> state.candidates[cell].size in 2..4 }
            val combinations: List<List<Int>> = combinationsOfFour(candidateCells)
            for (combination: List<Int> in combinations) {
                if (!isNakedQuad(combination, state)) continue
                val eliminations: Map<Int, Set<Int>> =
                    findNakedSubsetEliminations(unit, combination, state)
                if (eliminations.isEmpty()) continue
                return TechniqueSearchResult.Found(
                    LogicalStep(technique, eliminations = eliminations)
                )
            }
        }
        return TechniqueSearchResult.NotFound
    }

    private fun isNakedQuad(
        cells: List<Int>,
        state: LogicalState,
    ): Boolean {
        if (cells.size != 4 || cells.toSet().size != 4) return false

        val candidateSets: List<Set<Int>> = cells.map { cell: Int ->
            state.candidates[cell]
        }
        if (candidateSets.any { candidates: Set<Int> -> candidates.size !in 2..4 }) return false

        val combinedCandidates: Set<Int> = candidateSets.flatten().toSet()
        return combinedCandidates.size == 4
    }
}
