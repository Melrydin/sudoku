package sudoku.domain.grading.techniques

import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.LogicalStep
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class LockedCandidatesPointingDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.LOCKED_CANDIDATES_POINTING

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        val boxes: List<List<Int>> = SudokuUnits.boxes
        boxes.forEach { box: List<Int> ->
            for (digit: Int in 1..9) {
                val matchingCells: List<Int> = box.filter { cell: Int ->
                    digit in state.candidates[cell]
                }
                val row: Int =
                    matchingCells.map { cell: Int -> cell / SudokuBoard.SIZE }.distinct().singleOrNull() ?: -1
                val column: Int =
                    matchingCells.map { cell: Int -> cell % SudokuBoard.SIZE }.distinct().singleOrNull() ?: -1
                val eliminationCells: Set<Int> = findEliminationCells(state, box, row, column, digit)
                if (eliminationCells.isNotEmpty()) {
                    val eliminations: Map<Int, Set<Int>> = eliminationCells.associateWith { setOf(digit) }
                    return TechniqueSearchResult.Found(LogicalStep(technique, eliminations = eliminations))
                }
            }
        }
        return TechniqueSearchResult.NotFound
    }

    private fun findEliminationCells(
        state: LogicalState,
        box: List<Int>,
        row: Int,
        column: Int,
        digit: Int,
    ): Set<Int> {
        val line: List<Int> = when {
            column >= 0 -> SudokuUnits.columns[column]
            row >= 0 -> SudokuUnits.rows[row]
            else -> return emptySet()
        }
        return line.filter { cell: Int -> cell !in box && digit in state.candidates[cell] }.toSet()
    }
}
