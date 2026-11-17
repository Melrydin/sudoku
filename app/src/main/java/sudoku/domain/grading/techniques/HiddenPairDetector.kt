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

        units.forEach { unit ->
            for (digit in 1..9) {
                val candidateCells = unit.filter { cell -> digit in state.candidates[cell] }
                if (candidateCells.size != 2) continue

                val otherDigit = findMatchingDigit(unit, digit, candidateCells, state)

                if (otherDigit == 0) continue

                val pairDigits = setOf(digit, otherDigit)
                val eliminations = findEliminations(candidateCells, pairDigits, state)

                if (eliminations.isEmpty()) continue
                return TechniqueSearchResult.Found(
                    LogicalStep(
                        technique,
                        eliminations = eliminations
                    )
                )
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
        for (otherDigit in (digit + 1)..9) {
            val otherCells = unit.filter { cell ->
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
        val removableDigitsByCell = pairCells.associateWith { cell ->
            state.candidates[cell] - pairDigits
        }
        val eliminations = removableDigitsByCell.filterValues { digits ->
            digits.isNotEmpty()
        }
        return eliminations
    }
}
