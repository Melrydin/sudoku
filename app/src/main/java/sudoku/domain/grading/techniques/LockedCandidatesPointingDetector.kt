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
        val boxes = SudokuUnits.boxes
        boxes.forEach { box ->
            for (digit in 1..9) {
                val matchingCells = box.filter { cell ->
                    digit in state.candidates[cell]
                }
                val row = matchingCells.map { cell -> cell / SudokuBoard.SIZE }.distinct().singleOrNull()
                val column = matchingCells.map { cell -> cell % SudokuBoard.SIZE }.distinct().singleOrNull()
                val eliminationCells = findEliminationCells(state, box, row, column, digit)
                if (eliminationCells.isNotEmpty()) {
                    return TechniqueSearchResult.Found(
                        LogicalStep(technique, eliminations = eliminationCells.associateWith { setOf(digit) })
                    )
                }
            }
        }
        return TechniqueSearchResult.NotFound
    }

    private fun findEliminationCells(
        state: LogicalState,
        box: List<Int>,
        row: Int?,
        column: Int?,
        digit: Int,
    ): Set<Int> {
        val line = when {
            column != null -> SudokuUnits.columns[column]
            row != null -> SudokuUnits.rows[row]
            else -> return emptySet()
        }
        return line.filter { cell -> cell !in box && digit in state.candidates[cell] }.toSet()
    }
}
