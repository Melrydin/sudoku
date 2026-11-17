package sudoku.domain.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.data.puzzle.SamplePuzzle
import sudoku.domain.settings.GameOptions
import sudoku.fixtures.TestBoards

class GameOptionsTest {
    private val session: GameSession = GameSession(SamplePuzzle.create())
    private val enabled: GameOptions = GameOptions(false, false, true, false, false)
    private val disabled: GameOptions = enabled.copy(prefillNotes = false)

    @Test
    fun enablingPrefillUsesCurrentEntriesAndPreservesGameProgress() {
        session.select(2)
        session.enter(4)
        session.trackElapsedTime(1_234)
        val before: GameState = session.state
        session.applyOptions(enabled)
        assertEquals(before.copy(notes = GameNotes.prefill(before.board)), session.state)
        assertEquals(1_234L, session.elapsedMillis)
        assertEquals(0, session.mistakes)
        assertEquals(1, session.assistancePoints)
        assertTrue(session.prefillNotesEnabled)
        assertTrue(session.state.notes[2].isEmpty())
        assertEquals(setOf(2, 6), session.state.notes[3])
    }

    @Test
    fun applyingEnabledOptionsAgainPreservesManualNoteChanges() {
        session.applyOptions(enabled)
        session.select(2)
        session.erase()
        session.applyOptions(enabled)
        assertTrue(session.state.notes[2].isEmpty())
    }

    @Test
    fun disablingPrefillPreservesNotesAndReenablingRefillsThem() {
        session.applyOptions(enabled)
        session.select(2)
        session.erase()
        val edited: GameState = session.state
        session.applyOptions(disabled)
        assertEquals(edited, session.state)
        session.applyOptions(enabled)
        assertEquals(GameNotes.prefill(edited.board), session.state.notes)
        assertEquals(1, session.assistancePoints)
    }

    @Test
    fun prefillingCanBeUndoneWithoutRefundingAssistance() {
        session.select(2)
        session.toggleNotes()
        session.enter(1)
        val before: GameState = session.state
        session.applyOptions(enabled)
        session.undo()
        assertEquals(before, session.state)
        session.applyOptions(enabled)
        assertEquals(before, session.state)
        assertEquals(1, session.assistancePoints)
    }

    @Test
    fun newPrefilledGamesDoNotGainAnExtraUndoStep() {
        val game: GameSession = NewGameFactory.fromPuzzle(SamplePuzzle.create().puzzle, true)
        game.select(2)
        game.erase()
        game.applyOptions(enabled)
        assertTrue(game.state.notes[2].isEmpty())
        assertEquals(1, game.undoHistory.size)
    }

    @Test
    fun completedAndFailedGamesIgnorePrefilling() {
        val completed: GameSession = GameSession(GameState(TestBoards.solved()))
        val failed: GameSession = GameSession(SamplePuzzle.create(), initialMistakes = GameSession.MAX_MISTAKES)
        listOf(completed, failed).forEach { game: GameSession ->
            val before: GameState = game.state
            game.applyOptions(enabled)
            assertEquals(before, game.state)
            assertEquals(0, game.assistancePoints)
            assertTrue(!game.prefillNotesEnabled && !game.canUndo)
        }
    }
}
