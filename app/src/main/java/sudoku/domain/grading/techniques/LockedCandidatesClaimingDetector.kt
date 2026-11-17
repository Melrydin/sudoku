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
        val lines: List<List<Int>> = SudokuUnits.rows + SudokuUnits.columns
        for (line: List<Int> in lines) {
            for (digit: Int in 1..9) {
                val candidateCells: List<Int> = line.filter { cell: Int -> digit in state.candidates[cell] }
                if (candidateCells.size < 2) continue
                val eliminationCells: List<Int> = findEliminationCells(digit, line, candidateCells, state)

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
        val box: List<Int> = SudokuUnits.boxes.singleOrNull { box: List<Int> ->
            candidateCells.all { cell: Int -> cell in box }
        }.orEmpty()
        return box.filter { cell: Int -> cell !in line }
            .filter { cell: Int -> digit in state.candidates[cell] }.orEmpty()
    }
}
