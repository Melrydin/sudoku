package sudoku.fixtures

import sudoku.domain.board.SudokuBoard
import sudoku.domain.grading.LogicalState

object LogicalStates {
    fun fullHouse(vararg houses: Pair<List<Int>, Int>): LogicalState {
        val solution = TestBoards.solved()
        val filled = houses.flatMap { (unit, empty) -> unit.filter { it != empty } }.toSet()
        val board = SudokuBoard.from(solution.cells.indices.map { if (it in filled) solution[it] else 0 })
        return LogicalState.from(board)
    }

    fun withCandidates(state: LogicalState, vararg changes: Pair<Int, Set<Int>>): LogicalState {
        val replacements = changes.toMap()
        return state.copy(candidates = state.candidates.mapIndexed { cell, values -> replacements[cell] ?: values })
    }

    fun restrictDigit(state: LogicalState, unit: List<Int>, matching: List<Int>, digit: Int): LogicalState =
        state.copy(candidates = state.candidates.mapIndexed { cell, values ->
            if (cell in unit && cell !in matching) values - digit else values
        })
}
