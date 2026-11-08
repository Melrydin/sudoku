package com.example.sudoku.game

class SudokuBoard private constructor(val cells: List<Int>) {
    operator fun get(index: Int): Int = cells[index]

    fun withValue(index: Int, value: Int): SudokuBoard {
        require(index in cells.indices && value in 0..SIZE)
        return from(cells.toMutableList().apply { this[index] = value })
    }

    companion object {
        const val SIZE: Int = 9
        const val BOX_SIZE: Int = 3
        const val CELL_COUNT: Int = SIZE * SIZE

        fun from(cells: List<Int>): SudokuBoard {
            require(cells.size == CELL_COUNT && cells.all { it in 0..SIZE })
            return SudokuBoard(cells.toList())
        }

        fun parse(digits: String): SudokuBoard {
            require(digits.length == CELL_COUNT && digits.all { it in '0'..'9' })
            return from(digits.map { it.digitToInt() })
        }
    }
}
