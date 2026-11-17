package sudoku.domain.game

import sudoku.domain.common.OptionalValue
import sudoku.domain.puzzle.GeneratorVersion
import sudoku.domain.puzzle.PuzzleId

fun GameRepository.findSavedPuzzle(puzzle: PuzzleId): OptionalValue<PuzzleId> =
    GeneratorVersion.entries.asReversed()
        .filter { it.number <= puzzle.version.number }
        .map { puzzle.copy(version = it) }
        .firstOrNull { load(it).isPresent }.let { OptionalValue.fromNullable(it) }
