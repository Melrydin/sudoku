package sudoku.domain.solver

import sudoku.domain.board.SudokuBoard
import sudoku.domain.common.OptionalValue
import sudoku.domain.common.checkCancellation

internal class SolutionSearch(board: SudokuBoard) {
    private val cells: IntArray = board.cells.toIntArray()
    private val rows: IntArray = IntArray(SudokuBoard.SIZE)
    private val columns: IntArray = IntArray(SudokuBoard.SIZE)
    private val boxes: IntArray = IntArray(SudokuBoard.SIZE)
    private val valid: Boolean = initializeMasks()
    private var firstSolution: OptionalValue<SudokuBoard> = OptionalValue.Absent

    fun count(): Int = if (valid) search(2) else 0

    fun solution(): OptionalValue<SudokuBoard> {
        if (valid) search(1)
        return firstSolution
    }

    private fun initializeMasks(): Boolean {
        for (cell: Int in cells.indices) {
            val value: Int = cells[cell]
            if (value == 0) continue
            if (availableMask(cell) and (1 shl value) == 0) return false
            updateMasks(cell, value)
        }
        return true
    }

    private fun search(limit: Int): Int {
        checkCancellation()
        val cell: Int = mostConstrainedCell()
        if (cell < 0) return recordSolution()
        var candidates: Int = availableMask(cell)
        var count: Int = 0
        while (candidates != 0 && count < limit) {
            val bit: Int = candidates and -candidates
            val value: Int = Integer.numberOfTrailingZeros(bit)
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
        if (!firstSolution.isPresent) firstSolution = OptionalValue.Present(SudokuBoard.from(cells.toList()))
        return 1
    }

    private fun mostConstrainedCell(): Int {
        var selected: Int = -1
        var fewest: Int = SudokuBoard.SIZE + 1
        for (cell: Int in cells.indices) {
            if (cells[cell] != 0) continue
            val count: Int = Integer.bitCount(availableMask(cell))
            if (count < fewest) {
                selected = cell
                fewest = count
            }
            if (fewest == 0) break
        }
        return selected
    }

    private fun availableMask(cell: Int): Int {
        val used: Int =
            rows[cell / SudokuBoard.SIZE] or columns[cell % SudokuBoard.SIZE] or boxes[boxIndex(cell)]
        return ALL_DIGITS and used.inv()
    }

    // Each bit is toggled once when placing a digit and again when undoing that placement.
    private fun updateMasks(cell: Int, value: Int) {
        val bit: Int = 1 shl value
        val row: Int = cell / SudokuBoard.SIZE
        val column: Int = cell % SudokuBoard.SIZE
        val box: Int = boxIndex(cell)
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
