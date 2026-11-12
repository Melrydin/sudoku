package sudoku.domain.grading.techniques

import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.LogicalStep
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class LockedCandidatesClaimingDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.LOCKED_CANDIDATES_CLAIMING

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        val lines = SudokuUnits.rows + SudokuUnits.columns
        for (line in lines) {
            for (digit in 1..9) {
                val candidateCells = line.filter { cell -> digit in state.candidates[cell] }
                if (candidateCells.size < 2) continue
                val eliminationCells = findEliminationCells(digit, line, candidateCells, state)

                if (eliminationCells.isEmpty()) continue
                return TechniqueSearchResult.Found(
                    LogicalStep(
                        technique,
                        eliminations = eliminationCells.associateWith { setOf(digit) }
                    )
                )
            }
        }
        return TechniqueSearchResult.NotFound
    }

    private fun findEliminationCells(
        digit: Int,
        line: List<Int>,
        candidateCells: List<Int>,
        state: LogicalState,
    ): List<Int> {
        val box = SudokuUnits.boxes.singleOrNull { box ->
            candidateCells.all { cell -> cell in box }
        }
        return box?.filter { cell -> cell !in line }?.filter { cell -> digit in state.candidates[cell] }.orEmpty()
    }
}
