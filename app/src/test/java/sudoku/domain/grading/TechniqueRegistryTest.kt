package sudoku.domain.grading

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.puzzle.GeneratorVersion

class TechniqueRegistryTest {
    @Test
    fun injectedDetectorsFollowPolicyOrderRatherThanRegistrationOrder() {
        val fullHouse = detector(SolvingTechnique.FULL_HOUSE)
        val xWing = detector(SolvingTechnique.X_WING)
        val registry = TechniqueRegistry(listOf(xWing, fullHouse))
        val ordered = registry.ordered(GeneratorVersion.V1)
        assertSame(fullHouse, ordered.first())
        assertSame(xWing, ordered.single { it.technique == SolvingTechnique.X_WING })
        assertEquals(TechniqueSearchResult.NotFound, fullHouse.findStep(LogicalState.from(SudokuBoard.empty())))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDuplicateImplementationsOfTheSameTechnique() {
        TechniqueRegistry(listOf(detector(SolvingTechnique.FULL_HOUSE), detector(SolvingTechnique.FULL_HOUSE)))
    }

    private fun detector(kind: SolvingTechnique): TechniqueDetector = object : TechniqueDetector {
        override val technique: SolvingTechnique = kind

        override fun findStep(state: LogicalState): TechniqueSearchResult = TechniqueSearchResult.NotFound
    }
}
