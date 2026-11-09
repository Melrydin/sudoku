package sudoku.generator

import sudoku.game.SudokuBoard

class PuzzleGenerator(
    private val generateSolution: () -> SudokuBoard = { SudokuGenerator().generate() },
    private val remover: ClueRemover = ClueRemover(),
) {
    fun generate(targetClues: Int): ClueRemovalResult {
        require(targetClues in 0 until SudokuBoard.CELL_COUNT) { "A playable puzzle needs an empty cell." }
        return remover.removeClues(generateSolution(), targetClues)
    }
}
