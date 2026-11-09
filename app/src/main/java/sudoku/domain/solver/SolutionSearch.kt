package sudoku.domain.solver

import sudoku.domain.board.SudokuBoard
import sudoku.domain.common.checkCancellation

internal class SolutionSearch(board: SudokuBoard) {
    private val cells = board.cells.toIntArray()
    private val rows = IntArray(SudokuBoard.SIZE)
    private val columns = IntArray(SudokuBoard.SIZE)
    private val boxes = IntArray(SudokuBoard.SIZE)
    private val valid = initializeMasks()
    private var firstSolution: SudokuBoard? = null

    fun count(): Int = if (valid) search(2) else 0

    fun solution(): SudokuBoard? {
        if (valid) search(1)
        return firstSolution
    }

    private fun initializeMasks(): Boolean {
        for (cell in cells.indices) {
            val value = cells[cell]
            if (value == 0) continue
            if (availableMask(cell) and (1 shl value) == 0) return false
            updateMasks(cell, value)
        }
        return true
    }

    private fun search(limit: Int): Int {
        checkCancellation()
        val cell = mostConstrainedCell() ?: return recordSolution()
        var candidates = availableMask(cell)
        var count = 0
        while (candidates != 0 && count < limit) {
            val bit = candidates and -candidates
            val value = Integer.numberOfTrailingZeros(bit)
            cells[cell] = value
            updateMasks(cell, value)
            count += search(limit - count)
            updateMasks(cell, value)
            cells[cell] = 0
            candidates = candidates xor bit
        }
        return count
    }

    private fun recordSolution(): Int {
        if (firstSolution == null) firstSolution = SudokuBoard.from(cells.toList())
        return 1
    }

    private fun mostConstrainedCell(): Int? {
        var selected: Int? = null
        var fewest = SudokuBoard.SIZE + 1
        for (cell in cells.indices) {
            if (cells[cell] != 0) continue
            val count = Integer.bitCount(availableMask(cell))
            if (count < fewest) {
                selected = cell
                fewest = count
            }
            if (fewest == 0) break
        }
        return selected
    }

    private fun availableMask(cell: Int): Int {
        val used = rows[cell / SudokuBoard.SIZE] or columns[cell % SudokuBoard.SIZE] or boxes[boxIndex(cell)]
        return ALL_DIGITS and used.inv()
    }

    // Each bit is toggled once when placing a digit and again when undoing that placement.
    private fun updateMasks(cell: Int, value: Int) {
        val bit = 1 shl value
        val row = cell / SudokuBoard.SIZE
        val column = cell % SudokuBoard.SIZE
        val box = boxIndex(cell)
        rows[row] = rows[row] xor bit
        columns[column] = columns[column] xor bit
        boxes[box] = boxes[box] xor bit
    }

    private fun boxIndex(cell: Int): Int =
        cell / SudokuBoard.SIZE / SudokuBoard.BOX_SIZE * SudokuBoard.BOX_SIZE +
            cell % SudokuBoard.SIZE / SudokuBoard.BOX_SIZE

    companion object {
        private const val ALL_DIGITS: Int = (1 shl (SudokuBoard.SIZE + 1)) - 2
    }
}
