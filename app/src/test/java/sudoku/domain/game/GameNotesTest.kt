package sudoku.domain.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.data.puzzle.SamplePuzzle
import sudoku.domain.board.SudokuBoard
import sudoku.fixtures.TestBoards

class GameNotesTest {
    @Test
    fun prefillUsesOnlyRowColumnAndBoxConstraints() {
        val state = SamplePuzzle.create()
        val notes = GameNotes.prefill(state.board)
        assertEquals(setOf(1, 2, 4), notes[2])
        assertTrue(state.board.cells.indices.filter { state.board[it] != 0 }.all { notes[it].isEmpty() })
        assertFalse(state.hasEntries)
    }

    @Test
    fun emptyBoardAllowsAllDigitsAndFilledBoardHasNoNotes() {
        assertTrue(GameNotes.prefill(TestBoards.empty()).all { it == (1..9).toSet() })
        assertTrue(GameNotes.prefill(TestBoards.solved()).all { it.isEmpty() })
    }

    @Test
    fun entryRemovesOnlyMatchingNotesFromPeersAndUndoRestoresThem() {
        val state = notedState()
        val session = GameSession(state)
        session.enter(1, autoRemoveNotes = true)
        assertTrue(session.state.notes[0].isEmpty())
        listOf(8, 72, 10).forEach { assertEquals(setOf(2), session.state.notes[it]) }
        assertEquals(setOf(1, 2), session.state.notes[40])
        session.undo()
        assertEquals(state.notes, session.state.notes)
        assertEquals(state.board.cells, session.state.board.cells)
    }

    @Test
    fun disabledCleanupKeepsPeerNotes() {
        val session = GameSession(notedState())
        session.enter(1, autoRemoveNotes = false)
        listOf(8, 72, 10, 40).forEach { assertEquals(setOf(1, 2), session.state.notes[it]) }
        assertTrue(session.state.notes[0].isEmpty())
    }

    @Test
    fun enteringANoteDoesNotRemoveNotesFromPeers() {
        val session = GameSession(notedState().copy(noteMode = true))
        session.enter(1, autoRemoveNotes = true)
        assertEquals(setOf(2), session.state.notes[0])
        assertEquals(setOf(1, 2), session.state.notes[8])
        assertEquals(0, session.state.board[0])
    }

    @Test
    fun givensAndCompletedGamesIgnoreCleanup() {
        val session = GameSession(SamplePuzzle.create())
        session.select(0)
        session.enter(1, autoRemoveNotes = true)
        assertFalse(session.canUndo)
        assertEquals(5, session.state.board[0])
        val completed = GameSession(GameState(TestBoards.empty(), TestBoards.solved(), selectedCell = 0))
        completed.enter(1, autoRemoveNotes = true)
        assertFalse(completed.canUndo)
    }

    private fun notedState(): GameState = GameState(
        puzzle = TestBoards.empty(),
        notes = List(SudokuBoard.CELL_COUNT) { if (it in listOf(0, 8, 72, 10, 40)) setOf(1, 2) else emptySet() },
        selectedCell = 0,
    )
}
