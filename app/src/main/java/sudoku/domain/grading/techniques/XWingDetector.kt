package sudoku.domain.grading.techniques

import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.LogicalStep
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class XWingDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.X_WING

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        for (digit: Int in 1..SudokuBoard.SIZE) {
            val rowStep: TechniqueSearchResult =
                findStepInUnits(digit, SudokuUnits.rows, SudokuUnits.columns, state)
            if (rowStep is TechniqueSearchResult.Found) return rowStep
            val columnStep: TechniqueSearchResult =
                findStepInUnits(digit, SudokuUnits.columns, SudokuUnits.rows, state)
            if (columnStep is TechniqueSearchResult.Found) return columnStep
        }
        return TechniqueSearchResult.NotFound
    }

    private fun findStepInUnits(
        digit: Int,
        units: List<List<Int>>,
        crossUnits: List<List<Int>>,
        state: LogicalState,
    ): TechniqueSearchResult {
        val candidateUnits: List<List<Int>> = findCandidateUnits(digit, units, state)
        val xWingCandidates: List<List<Int>> = findXWing(digit, candidateUnits, state)
        for (xWing: List<Int> in xWingCandidates) {
            val eliminations: Map<Int, Set<Int>> = findEliminations(digit, xWing, crossUnits, state)
            if (eliminations.isEmpty()) continue
            return TechniqueSearchResult.Found(LogicalStep(technique, eliminations = eliminations))
        }
        return TechniqueSearchResult.NotFound
    }

    private fun findCandidateUnits(
        digit: Int,
        units: List<List<Int>>,
        state: LogicalState
    ): List<List<Int>> {
        val candidateUnits: MutableList<List<Int>> = mutableListOf()
        for (unit: List<Int> in units) {
            val candidateCells: List<Int> =
                unit.filter { cell: Int -> digit in state.candidates[cell] }
            if (candidateCells.size == 2) {
                candidateUnits.add(unit)
            }
        }
        return candidateUnits
    }

    private fun findXWing(digit: Int, units: List<List<Int>>, state: LogicalState): List<List<Int>> {
        val xWing: MutableList<List<Int>> = mutableListOf()
        for (first: Int in 0 until (units.size - 1)) {
            for (second: Int in (first + 1) until units.size) {
                val firstPositions: List<Int> = findCandidatePositions(digit, units[first], state)
                val secondPositions: List<Int> = findCandidatePositions(digit, units[second], state)
                if (firstPositions == secondPositions) {
                    val cornerCells: List<Int> = (units[first] + units[second]).filter { cell: Int ->
                        digit in state.candidates[cell]
                    }
                    xWing.add(cornerCells)
                }
            }
        }
        return xWing
    }

    private fun findCandidatePositions(
        digit: Int,
        unit: List<Int>,
        state: LogicalState
    ): List<Int> {
        return unit.indices.filter { position: Int -> digit in state.candidates[unit[position]] }
    }

    private fun findEliminations(
        digit: Int,
        xWing: List<Int>,
        crossUnits: List<List<Int>>,
        state: LogicalState,
    ): Map<Int, Set<Int>> {
        val eliminations: MutableMap<Int, Set<Int>> = mutableMapOf()
        for (unit: List<Int> in crossUnits) {
            if (unit.none { cell: Int -> cell in xWing }) continue
            for (cell: Int in unit) {
                if (cell in xWing) continue
                if (digit in state.candidates[cell]) {
                    eliminations[cell] = setOf(digit)
                }
            }
        }
        return eliminations
    }
}
