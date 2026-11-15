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
        val board = SudokuUnits.all

        board.forEach { unit ->
            val pairs = findPairs(unit, state)
            if (pairs.isNotEmpty()) {
                for (pair in pairs) {
                    val eliminations: Map<Int, Set<Int>> = findEliminations(unit, pair, state)
                    if (eliminations.isEmpty()) continue

                    return TechniqueSearchResult.Found(
                        LogicalStep(
                            technique,
                            eliminations = eliminations
                        )
                    )
                }
            }


        }
        return TechniqueSearchResult.NotFound
    }

    private fun findPairs(unit: List<Int>, state: LogicalState): List<List<Int>> {
        var matchingCells = unit.filter { cell -> state.candidates[cell].size == 2 }
        val pairs = mutableListOf<List<Int>>()

        while (matchingCells.isNotEmpty()) {
            val pair = searchPair(matchingCells, state)
            if (pair.isEmpty()) break

            pairs.add(pair)
            matchingCells = matchingCells.filter { cell -> cell !in pair }
        }
        return pairs
    }

    private fun searchPair(matchingCells: List<Int>, state: LogicalState): List<Int> {
        for (cell in matchingCells) {
            val pairDigits = state.candidates[cell]
            val matches = matchingCells.filter { otherCell ->
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
        val pairDigits = state.candidates[pair.first()]
        val withoutCurrentPair = unit.filter { cell -> cell !in pair }
        val removableDigitsByCell = withoutCurrentPair
            .associateWith { cell -> state.candidates[cell].intersect(pairDigits) }
        val eliminations = removableDigitsByCell.filterValues { digits -> digits.isNotEmpty() }
        return eliminations

    }
}
