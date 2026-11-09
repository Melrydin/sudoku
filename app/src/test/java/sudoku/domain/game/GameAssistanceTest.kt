package sudoku.domain.game

import org.junit.Assert.assertEquals
import org.junit.Test
import sudoku.data.puzzle.SamplePuzzle
import sudoku.domain.settings.GameOptions
import sudoku.fixtures.TestBoards

class GameAssistanceTest {
    private val session = GameSession(SamplePuzzle.create())
    private val disabled = GameOptions(false, false, false, false, false)

    @Test
    fun everyActivatedAssistCountsOnceUpToFivePoints() {
        session.recordAssistance(GameOptions(prefillNotes = true))
        repeat(5) { session.recordAssistance(GameOptions(prefillNotes = true)) }
        assertEquals(5, session.assistancePoints)
    }

    @Test
    fun turningAssistanceOffDoesNotRemovePreviouslyRecordedPoints() {
        session.recordAssistance(disabled.copy(highlightSameDigits = true))
        session.recordAssistance(disabled)
        session.recordAssistance(disabled.copy(prefillNotes = true))
        assertEquals(2, session.assistancePoints)
    }

    @Test
    fun visualGameplayOptionsAreNotScoredAsAssistance() {
        session.recordAssistance(disabled.copy(boardAnimations = true, showTimer = true, circleGivens = true))
        assertEquals(0, session.assistancePoints)
    }

    @Test
    fun completionFreezesTheAssistanceScore() {
        val game = GameSession(GameState(TestBoards.solved().withValue(80, 0)))
        game.recordAssistance(disabled.copy(showRemainingCount = true))
        game.select(80)
        game.enter(9)
        game.recordAssistance(GameOptions(prefillNotes = true))
        assertEquals(1, game.assistancePoints)
    }
}
