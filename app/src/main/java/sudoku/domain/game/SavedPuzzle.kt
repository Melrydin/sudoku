package sudoku.domain.game

import sudoku.domain.puzzle.GeneratorVersion
import sudoku.domain.puzzle.PuzzleId

fun GameRepository.findSavedPuzzle(puzzle: PuzzleId): PuzzleId? = GeneratorVersion.entries.asReversed()
    .filter { it.number <= puzzle.version.number }
    .map { puzzle.copy(version = it) }
    .firstOrNull { load(it) != null }
