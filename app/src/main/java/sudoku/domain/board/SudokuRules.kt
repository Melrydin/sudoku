package sudoku.domain.board

object SudokuRules {
    fun arePeers(first: Int, second: Int): Boolean {
        val firstRow = first / SudokuBoard.SIZE
        val secondRow = second / SudokuBoard.SIZE
        val firstColumn = first % SudokuBoard.SIZE
        val secondColumn = second % SudokuBoard.SIZE
        return firstRow == secondRow || firstColumn == secondColumn ||
            firstRow / SudokuBoard.BOX_SIZE == secondRow / SudokuBoard.BOX_SIZE &&
            firstColumn / SudokuBoard.BOX_SIZE == secondColumn / SudokuBoard.BOX_SIZE
    }

    fun conflicts(board: SudokuBoard): Set<Int> = board.cells.indices.filterTo(mutableSetOf()) { index ->
        board[index] != 0 && board.cells.indices.any { other ->
            other != index && board[other] == board[index] && arePeers(index, other)
        }
    }

    fun isComplete(board: SudokuBoard): Boolean = board.cells.none { it == 0 } && conflicts(board).isEmpty()
}
