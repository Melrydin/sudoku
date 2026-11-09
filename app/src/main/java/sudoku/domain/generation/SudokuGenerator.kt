package sudoku.domain.generation

import kotlin.random.Random
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuRules
import sudoku.domain.common.checkCancellation

const val seed: Int = GeneratorConfig.APP_SEED

class SudokuGenerator(private val random: Random = Random(seed)) {
    private val decisions = ArrayDeque<GenerationDecision>()
    var board: SudokuBoard = SudokuBoard.empty()
    var freeCells = (0 until SudokuBoard.CELL_COUNT).toMutableList()

    fun generate(): SudokuBoard {
        while (!finish()) {
            checkCancellation()
            check(fillRandomCell() || backtrack()) { "No valid completion exists for the current board." }
        }
        check(SudokuRules.isComplete(board)) { "The completed board contains conflicts." }
        return board
    }

    private fun fillRandomCell(): Boolean {
        if (freeCells.isEmpty()) return false
        val choices = freeCells.map { GenerationDecision(it, findCandidates(it)) }
        val fewest = choices.minOf { it.remainingCandidates.size }
        val selected = choices.filter { it.remainingCandidates.size == fewest }.random(random)
        return fillCell(selected.cell, selected.remainingCandidates)
    }

    private fun fillCell(cell: Int, candidates: List<Int>): Boolean {
        if (candidates.isEmpty()) return false
        val candidate = candidates.random(random)
        board = board.withValue(cell, candidate)
        freeCells.remove(cell)
        decisions.addLast(GenerationDecision(cell, candidates - candidate))
        return true
    }

    private fun backtrack(): Boolean {
        while (true) {
            checkCancellation()
            val decision = undoLastDecision() ?: return false
            if (fillCell(decision.cell, decision.remainingCandidates)) return true
        }
    }

    private fun undoLastDecision(): GenerationDecision? {
        val decision = decisions.removeLastOrNull() ?: return null
        removeCandidate(decision.cell)
        return decision
    }

    private fun findCandidates(cell: Int): List<Int> {
        val usedNumbers = rowNumbers(cell) + columnNumbers(cell) + boxNumbers(cell)
        return (1..SudokuBoard.SIZE).filter { it !in usedNumbers }
    }

    private fun rowNumbers(cell: Int): Set<Int> {
        val firstIndex = cell / SudokuBoard.SIZE * SudokuBoard.SIZE
        return numbersInCells(firstIndex until firstIndex + SudokuBoard.SIZE, cell)
    }

    private fun columnNumbers(cell: Int): Set<Int> {
        val column = cell % SudokuBoard.SIZE
        val indices = (0 until SudokuBoard.SIZE).map { row -> row * SudokuBoard.SIZE + column }
        return numbersInCells(indices, cell)
    }

    private fun boxNumbers(cell: Int): Set<Int> {
        val firstRow = cell / SudokuBoard.SIZE / SudokuBoard.BOX_SIZE * SudokuBoard.BOX_SIZE
        val firstColumn = cell % SudokuBoard.SIZE / SudokuBoard.BOX_SIZE * SudokuBoard.BOX_SIZE
        val indices = (firstRow until firstRow + SudokuBoard.BOX_SIZE).flatMap { row ->
            (firstColumn until firstColumn + SudokuBoard.BOX_SIZE).map { column ->
                row * SudokuBoard.SIZE + column
            }
        }
        return numbersInCells(indices, cell)
    }

    private fun numbersInCells(indices: Iterable<Int>, excludedCell: Int): Set<Int> {
        return indices.filter { it != excludedCell }
            .map { board[it] }
            .filter { it != 0 }
            .toSet()
    }

    private fun removeCandidate(cell: Int) {
        board = board.withValue(cell, 0)
        if (cell !in freeCells) freeCells.add(cell)
    }

    private fun finish(): Boolean {
        return board.cells.all { it != 0 }
    }
}
