package sudoku.game

object DigitCounts {
    fun remaining(board: SudokuBoard, digit: Int): Int {
        require(digit in 1..SudokuBoard.SIZE)
        return (SudokuBoard.SIZE - board.cells.count { it == digit }).coerceAtLeast(0)
    }
}
