package sudoku.domain.grading.techniques.helpers

import sudoku.domain.grading.LogicalState

internal fun findNakedSubsetEliminations(
    unit: List<Int>,
    subsetCells: List<Int>,
    state: LogicalState,
): Map<Int, Set<Int>> {
    val subsetDigits: Set<Int> = subsetCells.flatMap { cell: Int ->
        state.candidates[cell]
    }.toSet()
    val otherCells: List<Int> = unit.filter { cell: Int -> cell !in subsetCells }
    val removableDigitsByCell: Map<Int, Set<Int>> = otherCells.associateWith { cell: Int ->
        state.candidates[cell].intersect(subsetDigits)
    }
    val eliminations: Map<Int, Set<Int>> = removableDigitsByCell.filterValues { digits: Set<Int> ->
        digits.isNotEmpty()
    }
    return eliminations
}
