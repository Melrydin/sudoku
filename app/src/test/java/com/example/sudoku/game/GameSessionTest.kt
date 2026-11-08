package com.example.sudoku.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSessionTest {
    private val session = GameSession(SamplePuzzle.create())

    @Test
    fun startingNumbersCannotBeChangedOrErased() {
        session.select(0)
        session.enter(9)
        session.erase()
        assertEquals(5, session.state.board[0])
        assertFalse(session.canUndo)
    }

    @Test
    fun enteringWithoutSelectionDoesNothing() {
        session.enter(4)
        assertFalse(session.state.hasEntries)
        assertFalse(session.canUndo)
    }

    @Test
    fun entriesCanBeErasedAndUndone() {
        session.select(2)
        session.enter(4)
        session.erase()
        assertEquals(0, session.state.board[2])
        session.undo()
        assertEquals(4, session.state.board[2])
        session.undo()
        assertFalse(session.state.hasEntries)
    }

    @Test
    fun notesToggleWithoutChangingCellValues() {
        session.select(2)
        session.toggleNotes()
        session.enter(1)
        session.enter(4)
        session.enter(1)
        assertEquals(setOf(4), session.state.notes[2])
        assertEquals(0, session.state.board[2])
    }

    @Test
    fun entriesReplaceNotesAndUndoRestoresThem() {
        session.select(2)
        session.toggleNotes()
        session.enter(4)
        session.toggleNotes()
        session.enter(4)
        assertTrue(session.state.notes[2].isEmpty())
        session.undo()
        assertEquals(0, session.state.board[2])
        assertEquals(setOf(4), session.state.notes[2])
    }

    @Test
    fun notesCannotBeAddedToFilledCells() {
        session.select(2)
        session.enter(4)
        session.toggleNotes()
        session.enter(6)
        assertTrue(session.state.notes[2].isEmpty())
        assertEquals(4, session.state.board[2])
    }

    @Test
    fun conflictingEntriesRemainEditable() {
        session.select(2)
        session.enter(5)
        assertTrue(2 in SudokuRules.conflicts(session.state.board))
        session.enter(4)
        assertTrue(SudokuRules.conflicts(session.state.board).isEmpty())
    }

    @Test
    fun completedGamesIgnoreEditsButSupportUndo() {
        val game = GameSession(GameState(TestBoards.solved().withValue(80, 0)))
        game.select(80)
        game.enter(9)
        assertTrue(game.state.isComplete)
        game.enter(1)
        game.erase()
        assertEquals(9, game.state.board[80])
        game.undo()
        assertFalse(game.state.isComplete)
        assertEquals(0, game.state.board[80])
    }

    @Test
    fun historySurvivesAReconstructedSession() {
        session.select(2)
        session.enter(4)
        val restored = GameSession(session.state, session.undoHistory)
        restored.undo()
        assertEquals(0, restored.state.board[2])
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsRestoringChangedStartingNumbers() {
        val puzzle = SamplePuzzle.create().puzzle
        GameState(puzzle, puzzle.withValue(0, 9))
    }
}
