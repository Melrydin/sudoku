package com.example.sudoku.solver

import com.example.sudoku.game.SudokuBoard

sealed interface SolverResult {
    data class Solved(val board: SudokuBoard) : SolverResult
    data object Unsolvable : SolverResult
    data object NotImplemented : SolverResult
}
