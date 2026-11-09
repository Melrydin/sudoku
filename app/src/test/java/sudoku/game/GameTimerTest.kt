package sudoku.game

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class GameTimerTest {
    private var now: Long = 0
    private val timer = GameTimer(now = { now })

    @Test
    fun countsActiveTimeAndExcludesPausedTime() {
        timer.resume()
        now = 1_500
        timer.pause()
        now = 9_000
        assertEquals(1_500L, timer.elapsedMillis)
        timer.resume()
        now = 10_000
        assertEquals(2_500L, timer.elapsedMillis)
    }

    @Test
    fun repeatedResumeDoesNotResetTheStart() {
        timer.resume()
        now = 1_000
        timer.resume()
        now = 2_000
        assertEquals(2_000L, timer.elapsedMillis)
    }

    @Test
    fun repeatedPauseDoesNotAddTime() {
        timer.resume()
        now = 1_000
        timer.pause()
        now = 2_000
        timer.pause()
        assertEquals(1_000L, timer.elapsedMillis)
    }

    @Test
    fun restoredTimerStartsPausedAndContinuesFromSavedTime() {
        val restored = GameTimer(60_000) { now }
        now = 10_000
        assertEquals(60_000L, restored.elapsedMillis)
        restored.resume()
        now = 11_500
        assertEquals(61_500L, restored.elapsedMillis)
    }

    @Test
    fun formatsMinutesAndHoursWithoutWrappingAtOneHour() {
        assertEquals("00:00", PlayTimeFormatter.format(0, Locale.ROOT))
        assertEquals("59:59", PlayTimeFormatter.format(3_599_999, Locale.ROOT))
        assertEquals("1:00:00", PlayTimeFormatter.format(3_600_000, Locale.ROOT))
        assertEquals("24:00:01", PlayTimeFormatter.format(86_401_000, Locale.ROOT))
    }

    @Test
    fun undoKeepsElapsedTime() {
        val session = GameSession(SamplePuzzle.create())
        session.select(2)
        session.enter(4)
        session.trackElapsedTime(25_000)
        session.undo()
        assertEquals(25_000L, session.elapsedMillis)
    }
}
