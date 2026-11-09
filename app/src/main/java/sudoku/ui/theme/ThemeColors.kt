package sudoku.ui.theme

import android.content.Context
import com.google.android.material.color.MaterialColors
import sudoku.R

class ThemeColors(context: Context) {
    val foreground: Int = MaterialColors.getColor(context, R.attr.homeForeground, 0)
    val muted: Int = MaterialColors.getColor(context, R.attr.homeMuted, 0)
    val accent: Int = MaterialColors.getColor(context, R.attr.homeAccent, 0)
    val background: Int = MaterialColors.getColor(context, R.attr.homeBackground, 0)
    val outline: Int = MaterialColors.getColor(context, R.attr.homeOutline, 0)
    val error: Int = MaterialColors.getColor(context, com.google.android.material.R.attr.colorError, 0)
}
