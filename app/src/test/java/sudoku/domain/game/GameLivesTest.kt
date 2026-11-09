package sudoku.domain.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.data.puzzle.SamplePuzzle
import sudoku.domain.board.SudokuRules

class GameLivesTest {
    private val session = GameSession(SamplePuzzle.create())

    @Test
    fun aWrongButConflictFreeEntryCostsOneLife() {
        session.select(2)
        session.enter(1)
        assertTrue(SudokuRules.conflicts(session.state.board).isEmpty())
        assertEquals(setOf(2), session.incorrectCells)
        assertEquals(2, session.remainingLives)
    }

    @Test
    fun correctEntriesAndAttemptsToEditGivensDoNotCostLives() {
        session.select(2)
        session.enter(4)
        session.select(0)
        session.enter(1)
        assertEquals(3, session.remainingLives)
        assertTrue(session.incorrectCells.isEmpty())
    }

    @Test
    fun notesNeverCostLives() {
        session.select(2)
        session.toggleNotes()
        listOf(1, 2, 5, 9).forEach { session.enter(it) }
        assertEquals(3, session.remainingLives)
        assertFalse(session.isGameOver)
    }

    @Test
    fun erasingAndUndoDoNotRefundLives() {
        session.select(2)
        session.enter(1)
        session.erase()
        session.undo()
        session.undo()
        assertEquals(2, session.remainingLives)
        assertEquals(0, session.state.board[2])
    }

    @Test
    fun everyWrongSubmissionCountsEvenWhenTheValueIsUnchanged() {
        session.select(2)
        session.enter(1)
        session.enter(1)
        assertEquals(1, session.remainingLives)
    }

    @Test
    fun theThirdMistakeClearsProgressAndStopsFurtherActions() {
        session.select(2)
        session.trackElapsedTime(15_000)
        repeat(3) { session.enter(1) }
        session.select(3)
        session.toggleNotes()
        session.enter(6)
        session.undo()
        session.trackElapsedTime(20_000)
        assertTrue(session.isGameOver)
        assertEquals(0, session.remainingLives)
        assertEquals(session.state.puzzle.cells, session.state.board.cells)
        assertFalse(session.state.hasEntries)
        assertFalse(session.canUndo)
        assertEquals(0L, session.elapsedMillis)
    }

    @Test
    fun reconstructingASessionKeepsItsMistakesAndSolution() {
        session.select(2)
        session.enter(1)
        val restored = GameSession(session.state, session.undoHistory,
            initialSolution = session.solution, initialMistakes = session.mistakes)
        restored.undo()
        assertEquals(2, restored.remainingLives)
        assertEquals(session.solution.cells, restored.solution.cells)
    }
}
