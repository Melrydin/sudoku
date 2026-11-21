package sudoku.domain.grading.techniques.helpers

import sudoku.domain.grading.LogicalState

internal fun findEliminations(
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
