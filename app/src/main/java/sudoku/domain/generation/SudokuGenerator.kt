package sudoku.domain.generation

import kotlin.random.Random
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuRules
import sudoku.domain.common.OptionalValue
import sudoku.domain.common.checkCancellation

const val seed: Int = GeneratorConfig.APP_SEED

class SudokuGenerator(private val random: Random = Random(seed)) {
    private val decisions: ArrayDeque<GenerationDecision> = ArrayDeque<GenerationDecision>()
    var board: SudokuBoard = SudokuBoard.empty()
    var freeCells: MutableList<Int> = (0 until SudokuBoard.CELL_COUNT).toMutableList()

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
        val choices: List<GenerationDecision> = freeCells.map { GenerationDecision(it, findCandidates(it)) }
        val fewest: Int = choices.minOf { it.remainingCandidates.size }
        val selected: GenerationDecision = choices.filter { it.remainingCandidates.size == fewest }.random(random)
        return fillCell(selected.cell, selected.remainingCandidates)
    }

    private fun fillCell(cell: Int, candidates: List<Int>): Boolean {
        if (candidates.isEmpty()) return false
        val candidate: Int = candidates.random(random)
        board = board.withValue(cell, candidate)
        freeCells.remove(cell)
        decisions.addLast(GenerationDecision(cell, candidates - candidate))
        return true
    }

    private fun backtrack(): Boolean {
        while (true) {
            checkCancellation()
            val decision: GenerationDecision = undoLastDecision().getOrElse { return false }
            if (fillCell(decision.cell, decision.remainingCandidates)) return true
        }
    }

    private fun undoLastDecision(): OptionalValue<GenerationDecision> {
        if (decisions.isEmpty()) return OptionalValue.Absent
        val decision: GenerationDecision = decisions.removeLast()
        removeCandidate(decision.cell)
        return OptionalValue.Present(decision)
    }

    private fun findCandidates(cell: Int): List<Int> {
        val usedNumbers: Set<Int> = rowNumbers(cell) + columnNumbers(cell) + boxNumbers(cell)
        return (1..SudokuBoard.SIZE).filter { it !in usedNumbers }
    }

    private fun rowNumbers(cell: Int): Set<Int> {
        val firstIndex: Int = cell / SudokuBoard.SIZE * SudokuBoard.SIZE
        return numbersInCells(firstIndex until firstIndex + SudokuBoard.SIZE, cell)
    }

    private fun columnNumbers(cell: Int): Set<Int> {
        val column: Int = cell % SudokuBoard.SIZE
        val indices: List<Int> = (0 until SudokuBoard.SIZE).map { row: Int -> row * SudokuBoard.SIZE + column }
        return numbersInCells(indices, cell)
    }

    private fun boxNumbers(cell: Int): Set<Int> {
        val firstRow: Int = cell / SudokuBoard.SIZE / SudokuBoard.BOX_SIZE * SudokuBoard.BOX_SIZE
        val firstColumn: Int = cell % SudokuBoard.SIZE / SudokuBoard.BOX_SIZE * SudokuBoard.BOX_SIZE
        val indices: List<Int> = (firstRow until firstRow + SudokuBoard.BOX_SIZE).flatMap { row: Int ->
            (firstColumn until firstColumn + SudokuBoard.BOX_SIZE).map { column: Int ->
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
