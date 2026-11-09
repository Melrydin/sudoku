package sudoku.domain.grading

import sudoku.domain.board.SudokuBoard
import sudoku.domain.puzzle.GeneratorVersion

class DifficultyAnalyzer(private val registry: TechniqueRegistry = TechniqueRegistry()) {
    fun analyze(board: SudokuBoard, version: GeneratorVersion = GeneratorVersion.V1): GradingResult {
        // TODO: Initialize LogicalState once and try registry.ordered(version) from the beginning after each step.
        // TODO: Apply only effective steps and preserve all prior candidate eliminations.
        // TODO: Return Graded with the highest used technique tier only after solving, otherwise return Unrated.
        return GradingResult.NotImplemented
    }
}
