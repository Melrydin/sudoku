package sudoku.ui.common

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayTimeFormatterTest {
    @Test
    fun formatsMinutesAndHoursWithoutWrappingAtOneHour() {
        assertEquals("00:00", PlayTimeFormatter.format(0, Locale.ROOT))
        assertEquals("59:59", PlayTimeFormatter.format(3_599_999, Locale.ROOT))
        assertEquals("1:00:00", PlayTimeFormatter.format(3_600_000, Locale.ROOT))
        assertEquals("24:00:01", PlayTimeFormatter.format(86_401_000, Locale.ROOT))
    }
}
