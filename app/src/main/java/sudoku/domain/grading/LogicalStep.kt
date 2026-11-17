package sudoku.domain.grading

import sudoku.domain.board.SudokuBoard

data class LogicalStep(
    val technique: SolvingTechnique,
    val placements: Map<Int, Int> = emptyMap(),
    val eliminations: Map<Int, Set<Int>> = emptyMap(),
) {
    init {
        require(placements.isNotEmpty() || eliminations.isNotEmpty())
        require((placements.keys + eliminations.keys).all { it in 0 until SudokuBoard.CELL_COUNT })
        require(placements.values.all { it in 1..SudokuBoard.SIZE })
        require(eliminations.values.all { it.isNotEmpty() && it.all { digit: Int -> digit in 1..SudokuBoard.SIZE } })
        require(placements.keys.intersect(eliminations.keys).isEmpty())
    }

    fun isApplicableTo(state: LogicalState): Boolean = placements.all { (cell: Int, digit: Int) ->
        state.board[cell] == 0 && digit in state.candidates[cell]
    } && eliminations.all { (cell: Int, digits: Set<Int>) ->
        state.board[cell] == 0 && state.candidates[cell].containsAll(digits)
    }
}
