package sudoku.testing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import sudoku.domain.grading.LogicalState
import sudoku.domain.grading.SolvingTechnique
import sudoku.domain.grading.TechniqueDetector
import sudoku.domain.grading.TechniqueSearchResult

class TechniqueAssertions(private val detector: TechniqueDetector, private val technique: SolvingTechnique) {
    fun assertPlacement(state: LogicalState, cell: Int, digit: Int) {
        val result = findWithoutChangingInput(state)
        assertTrue("Expected a placement, got $result", result is TechniqueSearchResult.Found)
        val step = (result as TechniqueSearchResult.Found).step
        assertEquals(technique, step.technique)
        assertEquals(mapOf(cell to digit), step.placements)
        assertTrue(step.eliminations.isEmpty())
        assertTrue(step.isApplicableTo(state))
    }

    fun assertNotFound(state: LogicalState) {
        assertEquals(TechniqueSearchResult.NotFound, findWithoutChangingInput(state))
    }

    fun assertEliminations(state: LogicalState, expected: Map<Int, Set<Int>>) {
        val result = findWithoutChangingInput(state)
        assertTrue("Expected eliminations, got $result", result is TechniqueSearchResult.Found)
        val step = (result as TechniqueSearchResult.Found).step
        assertEquals(technique, step.technique)
        assertEquals(expected, step.eliminations)
        assertTrue(step.placements.isEmpty())
        assertTrue(step.isApplicableTo(state))
    }

    private fun findWithoutChangingInput(state: LogicalState): TechniqueSearchResult {
        val boardBefore = state.board.cells.toList()
        val candidatesBefore = state.candidates.map { it.toSet() }
        val result = detector.findStep(state)
        assertEquals(boardBefore, state.board.cells)
        assertEquals(candidatesBefore, state.candidates)
        return result
    }
}
