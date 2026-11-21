package sudoku.domain.grading.techniques.helpers

import sudoku.domain.grading.LogicalState

internal fun findCandidateCells(
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
