package sudoku.domain.grading.techniques

import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.LogicalStep
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult
import sudoku.domain.grading.techniques.helpers.combinationsOfThree

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

    private fun findCandidateCells(
        unit: List<Int>,
        combination: List<Int>,
        state: LogicalState,
    ): Set<Int> {
        val candidateCells: MutableSet<Int> = mutableSetOf()
        for (digit: Int in combination) {
            val digitCells: List<Int> = unit.filter { cell: Int -> digit in state.candidates[cell] }
            if (digitCells.isEmpty()) return emptySet()
            candidateCells.addAll(digitCells)
        }
        return candidateCells
    }

    private fun findEliminations(
        combination: List<Int>,
        cells: Set<Int>,
        state: LogicalState
    ): Map<Int, Set<Int>> {
        val eliminations: MutableMap<Int, Set<Int>> = mutableMapOf()
        for (cell: Int in cells) {
            val removableDigits: Set<Int> = state.candidates[cell].filter { candidate: Int ->
                candidate !in combination
            }.toSet()
            if (removableDigits.isNotEmpty()) eliminations[cell] = removableDigits
        }
        return eliminations
    }
}
