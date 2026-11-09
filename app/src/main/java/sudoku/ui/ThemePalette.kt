package sudoku.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import sudoku.game.GameColors
import sudoku.home.HomeTheme

class ThemePalette(
    private val context: Context,
    private val selected: HomeTheme,
    private val onSelected: (HomeTheme) -> Unit,
) {
    private val colors = GameColors(context)
    private val row = LinearLayout(context)
    private val popup = PopupWindow(row, dp(56 * HomeTheme.entries.size + 8), dp(56), true)

    fun show(anchor: View) {
        row.gravity = Gravity.CENTER
        row.background = panel()
        HomeTheme.entries.forEach { row.addView(themeButton(it)) }
        popup.setBackgroundDrawable(panel())
        popup.isOutsideTouchable = true
        popup.elevation = dp(4).toFloat()
        val position = IntArray(2)
        anchor.getLocationOnScreen(position)
        val x = (position[0] + anchor.width - popup.width).coerceAtLeast(dp(8))
        popup.showAtLocation(anchor, Gravity.TOP or Gravity.START, x, position[1])
    }

    private fun themeButton(theme: HomeTheme): ImageButton = ImageButton(context).apply {
        layoutParams = LinearLayout.LayoutParams(dp(56), dp(56))
        val preview = GameColors(ContextThemeWrapper(context, theme.style))
        val outer = circle(preview.background).apply {
            setStroke(dp(if (theme == selected) 3 else 1), preview.accent)
        }
        background = LayerDrawable(arrayOf(outer, circle(preview.accent))).apply {
            setLayerInset(0, dp(7), dp(7), dp(7), dp(7))
            setLayerInset(1, dp(21), dp(21), dp(21), dp(21))
        }
        contentDescription = context.getString(theme.label)
        isSelected = theme == selected
        setOnClickListener { popup.dismiss(); onSelected(theme) }
    }

    private fun panel(): GradientDrawable = GradientDrawable().apply {
        setColor(this@ThemePalette.colors.background)
        cornerRadius = dp(28).toFloat()
        setStroke(dp(1), this@ThemePalette.colors.outline)
    }

    private fun circle(color: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).toInt()
}
