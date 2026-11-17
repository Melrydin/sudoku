package sudoku.domain.grading.techniques

import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.LogicalStep
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class NakedPairDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.NAKED_PAIR

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        val board: List<List<Int>> = SudokuUnits.all

        board.forEach { unit: List<Int> ->
            val pairs: List<List<Int>> = findPairs(unit, state)
            if (pairs.isNotEmpty()) {
                for (pair: List<Int> in pairs) {
                    val eliminations: Map<Int, Set<Int>> = findEliminations(unit, pair, state)
                    if (eliminations.isEmpty()) continue

                    return TechniqueSearchResult.Found(LogicalStep(technique, eliminations = eliminations))
                }
            }


        }
        return TechniqueSearchResult.NotFound
    }

    private fun findPairs(unit: List<Int>, state: LogicalState): List<List<Int>> {
        var matchingCells: List<Int> = unit.filter { cell: Int -> state.candidates[cell].size == 2 }
        val pairs: MutableList<List<Int>> = mutableListOf<List<Int>>()

        while (matchingCells.isNotEmpty()) {
            val pair: List<Int> = searchPair(matchingCells, state)
            if (pair.isEmpty()) break

            pairs.add(pair)
            matchingCells = matchingCells.filter { cell: Int -> cell !in pair }
        }
        return pairs
    }

    private fun searchPair(matchingCells: List<Int>, state: LogicalState): List<Int> {
        for (cell: Int in matchingCells) {
            val pairDigits: Set<Int> = state.candidates[cell]
            val matches: List<Int> = matchingCells.filter { otherCell: Int ->
                state.candidates[otherCell] == pairDigits
            }
            if (matches.size == 2) return matches
        }
        return emptyList()
    }

    private fun findEliminations(
        unit: List<Int>,
        pair: List<Int>,
        state: LogicalState
    ): Map<Int, Set<Int>> {
        val pairDigits: Set<Int> = state.candidates[pair.first()]
        val withoutCurrentPair: List<Int> = unit.filter { cell: Int -> cell !in pair }
        val removableDigitsByCell: Map<Int, Set<Int>> = withoutCurrentPair
            .associateWith { cell: Int -> state.candidates[cell].intersect(pairDigits) }
        val eliminations: Map<Int, Set<Int>> =
            removableDigitsByCell.filterValues { digits: Set<Int> -> digits.isNotEmpty() }
        return eliminations

    }
}
