package sudoku.domain.board



object SudokuUnits {
    val rows: List<List<Int>> = List(SudokuBoard.SIZE) { row: Int ->
        List(SudokuBoard.SIZE) { column: Int -> row * SudokuBoard.SIZE + column }
    }

    val columns: List<List<Int>> = List(SudokuBoard.SIZE) { column: Int ->
        List(SudokuBoard.SIZE) { row: Int -> row * SudokuBoard.SIZE + column }
    }

    val boxes: List<List<Int>> = List(SudokuBoard.SIZE, ::boxCells)
    val all: List<List<Int>> = rows + columns + boxes

    private fun boxCells(box: Int): List<Int> {
        val firstRow: Int = box / SudokuBoard.BOX_SIZE * SudokuBoard.BOX_SIZE
        val firstColumn: Int = box % SudokuBoard.BOX_SIZE * SudokuBoard.BOX_SIZE
        return List(SudokuBoard.SIZE) { offset: Int ->
            val row: Int = firstRow + offset / SudokuBoard.BOX_SIZE
            val column: Int = firstColumn + offset % SudokuBoard.BOX_SIZE
            row * SudokuBoard.SIZE + column
        }
    }
}
