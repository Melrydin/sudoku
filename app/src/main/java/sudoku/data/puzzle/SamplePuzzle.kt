package sudoku.data.puzzle

import sudoku.domain.board.SudokuBoard
import sudoku.domain.game.GameState

object SamplePuzzle {
    fun create(): GameState = GameState(SudokuBoard.parse(
        "530070000" +
            "600195000" +
            "098000060" +
            "800060003" +
            "400803001" +
            "700020006" +
            "060000280" +
            "000419005" +
            "000080079",
    ))
}
