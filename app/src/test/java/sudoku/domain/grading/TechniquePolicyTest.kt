package sudoku.domain.grading

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.GeneratorVersion

class TechniquePolicyTest {
    private val version = GeneratorVersion.V1

    @Test
    fun versionOneContainsAllFourteenTechniquesInTheAgreedOrder() {
        val techniques = TechniquePolicy.rules(version).map { it.technique }
        assertEquals(14, techniques.size)
        assertEquals(SolvingTechnique.entries.toList(), techniques)
        assertEquals(techniques.size, techniques.distinct().size)
    }

    @Test
    fun eachDifficultyIncludesAllLowerTiers() {
        val expectedSizes = listOf(2, 3, 7, 10, 14)
        Difficulty.entries.forEachIndexed { index, difficulty ->
            val allowed = TechniquePolicy.allowed(version, difficulty)
            assertEquals(expectedSizes[index], allowed.size)
            assertEquals(TechniquePolicy.rules(version).take(allowed.size), allowed)
        }
    }

    @Test
    fun hardAndHighestTierRemainDistinct() {
        val hard = TechniquePolicy.allowed(version, Difficulty.HARD).map { it.technique }
        val highest = TechniquePolicy.allowed(version, Difficulty.EXTREME).map { it.technique }
        assertTrue(SolvingTechnique.X_WING in hard)
        assertTrue(SolvingTechnique.XY_WING !in hard)
        assertTrue(SolvingTechnique.XY_WING in highest)
    }
}
