package sudoku.domain.board

object SudokuUnits {
    val rows: List<List<Int>> = List(SudokuBoard.SIZE) { row ->
        List(SudokuBoard.SIZE) { column -> row * SudokuBoard.SIZE + column }
    }

    val columns: List<List<Int>> = List(SudokuBoard.SIZE) { column ->
        List(SudokuBoard.SIZE) { row -> row * SudokuBoard.SIZE + column }
    }

    val boxes: List<List<Int>> = List(SudokuBoard.SIZE, ::boxCells)
    val all: List<List<Int>> = rows + columns + boxes

    private fun boxCells(box: Int): List<Int> {
        val firstRow = box / SudokuBoard.BOX_SIZE * SudokuBoard.BOX_SIZE
        val firstColumn = box % SudokuBoard.BOX_SIZE * SudokuBoard.BOX_SIZE
        return List(SudokuBoard.SIZE) { offset ->
            val row = firstRow + offset / SudokuBoard.BOX_SIZE
            val column = firstColumn + offset % SudokuBoard.BOX_SIZE
            row * SudokuBoard.SIZE + column
        }
    }
}
