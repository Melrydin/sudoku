package sudoku.domain.grading.techniques

import sudoku.domain.board.SudokuUnits
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.LogicalStep
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class HiddenPairDetector : TechniqueDetector {
    override val technique: SolvingTechnique = SolvingTechnique.HIDDEN_PAIR

    override fun findStep(state: LogicalState): TechniqueSearchResult {
        val units: List<List<Int>> = SudokuUnits.all
        units.forEach { unit: List<Int> ->
            for (digit: Int in 1..9) {
                val candidateCells: List<Int> = unit.filter { cell: Int -> digit in state.candidates[cell] }
                if (candidateCells.size != 2) continue

                val otherDigit: Int = findMatchingDigit(unit, digit, candidateCells, state)
                if (otherDigit == 0) continue

                val pairDigits: Set<Int> = setOf(digit, otherDigit)
                val eliminations: Map<Int, Set<Int>> = findEliminations(candidateCells, pairDigits, state)

                if (eliminations.isEmpty()) continue
                return TechniqueSearchResult.Found(LogicalStep(technique, eliminations = eliminations))
            }
        }
        return TechniqueSearchResult.NotFound
    }

    private fun findMatchingDigit(
        unit: List<Int>,
        digit: Int,
        candidateCells: List<Int>,
        state: LogicalState,
    ): Int {
        for (otherDigit: Int in (digit + 1)..9) {
            val otherCells: List<Int> = unit.filter { cell: Int ->
                otherDigit in state.candidates[cell]
            }
            if (otherCells == candidateCells) return otherDigit
        }
        return 0
    }

    private fun findEliminations(
        pairCells: List<Int>,
        pairDigits: Set<Int>,
        state: LogicalState,
    ): Map<Int, Set<Int>> {
        val removableDigitsByCell: Map<Int, Set<Int>> = pairCells.associateWith { cell: Int ->
            state.candidates[cell] - pairDigits
        }
        val eliminations: Map<Int, Set<Int>> = removableDigitsByCell.filterValues { digits: Set<Int> ->
            digits.isNotEmpty()
        }
        return eliminations
    }
}
