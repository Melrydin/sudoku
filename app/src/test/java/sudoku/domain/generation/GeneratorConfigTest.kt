package sudoku.domain.generation

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Test
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.GeneratorVersion
import sudoku.domain.puzzle.PuzzleId

class GeneratorConfigTest {
    @Test
    fun seededTargetsCoverEveryValueIncludingBothRangeBounds() {
        Difficulty.entries.forEach { difficulty: Difficulty ->
            val puzzle: PuzzleId = PuzzleId(difficulty, version = GeneratorVersion.V2)
            val random: Random = Random(42)
            val targets: List<Int> = List(100) { GeneratorConfig.targetClues(puzzle, random) }
            assertEquals(GeneratorConfig.clueRange(difficulty).toSet(), targets.toSet())
        }
    }

    @Test
    fun legacyTargetsKeepTheRandomStreamUntouched() {
        val random: Random = Random(42)
        Difficulty.entries.forEach { difficulty: Difficulty ->
            assertEquals(
                36,
                GeneratorConfig.targetClues(
                    PuzzleId(difficulty, version = GeneratorVersion.V1),
                    random
                )
            )
        }
        assertEquals(Random(42).nextInt(), random.nextInt())
    }
}
