package sudoku.ui.common

import java.util.Locale

object PlayTimeFormatter {
    fun format(elapsedMillis: Long, locale: Locale = Locale.getDefault()): String {
        val seconds = elapsedMillis.coerceAtLeast(0) / 1_000
        val minutes = seconds / 60
        return if (minutes < 60) String.format(locale, "%02d:%02d", minutes, seconds % 60)
        else String.format(locale, "%d:%02d:%02d", minutes / 60, minutes % 60, seconds % 60)
    }
}
