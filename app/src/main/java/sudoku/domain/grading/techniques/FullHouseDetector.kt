package sudoku.domain.grading.techniques

import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.LogicalStep
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class FullHouseDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.FULL_HOUSE

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        val all = SudokuUnits.all

        all.forEach { unit ->
            val emptyCells = unit.filter { cell -> state.board[cell] == 0 }
            if (emptyCells.size == 1 && !foundDuplicates(unit, state)) {
                val cell = emptyCells.single()
                val candidate = state.candidates[cell].singleOrNull()
                    ?: return@forEach
                return TechniqueSearchResult.Found(
                    LogicalStep(
                        technique,
                        mapOf(cell to candidate),
                    ),
                )
            }
        }
        return TechniqueSearchResult.NotFound
    }

    private fun foundDuplicates(candidate: List<Int>, state: LogicalState): Boolean {
        val number = candidate
            .map { cell -> state.board[cell] }
            .filter { value -> value != 0 }
        val numbers = mutableSetOf<Int>()
        number.forEach { number ->
            if (number !in numbers) {
                numbers.add(number)
            } else {
                return true
            }
        }
        return false
    }
}
