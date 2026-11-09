package sudoku.generator

import sudoku.game.SudokuBoard

sealed interface ClueRemovalResult {
    data class Removed(val puzzle: SudokuBoard, val targetReached: Boolean) : ClueRemovalResult
    data object CounterRequired : ClueRemovalResult
}
