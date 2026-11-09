package sudoku.game

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.graphics.Typeface
import android.widget.GridLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.isInvisible
import sudoku.R
import sudoku.ui.formatNumber
import com.google.android.material.button.MaterialButton

class NumberPad(private val root: GridLayout, private val onNumber: (Int) -> Unit) {
    private val colors = GameColors(root.context)
    private val numbers = (1..SudokuBoard.SIZE).map { numberButton(it) }

    fun bind(onErase: () -> Unit) {
        numbers.forEach(root::addView)
        root.addView(eraseButton(onErase))
    }

    fun render(board: SudokuBoard, options: GameOptions) {
        numbers.forEachIndexed { index, button ->
            val remaining = DigitCounts.remaining(board, index + 1)
            val digit = root.context.formatNumber(index + 1)
            button.isInvisible = options.hideCompletedDigits && remaining == 0
            button.text = if (options.showRemainingCount) countedLabel(digit, remaining) else digit
            button.contentDescription = if (options.showRemainingCount) root.resources.getQuantityString(
                R.plurals.enter_number_remaining, remaining, digit, remaining,
            ) else root.context.getString(R.string.enter_number, digit)
        }
    }

    private fun countedLabel(digit: String, remaining: Int): SpannableString {
        val label = SpannableString("$digit\n${root.context.formatNumber(remaining)}")
        val flags = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        label.setSpan(RelativeSizeSpan(0.44f), digit.length + 1, label.length, flags)
        label.setSpan(ForegroundColorSpan(colors.muted), digit.length + 1, label.length, flags)
        return label
    }

    private fun numberButton(number: Int): MaterialButton = button().apply {
        text = context.formatNumber(number)
        isAllCaps = false
        typeface = Typeface.DEFAULT_BOLD
        tag = "number_$number"
        contentDescription = context.getString(R.string.enter_number, text)
        setOnClickListener { onNumber(number) }
    }

    private fun eraseButton(onErase: () -> Unit): MaterialButton = button().apply {
        id = R.id.erase
        icon = androidx.appcompat.content.res.AppCompatResources.getDrawable(context, R.drawable.ic_erase)
        iconSize = (26 * resources.displayMetrics.density).toInt()
        iconTint = android.content.res.ColorStateList.valueOf(colors.foreground)
        iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
        iconPadding = 0
        contentDescription = context.getString(R.string.erase)
        setOnClickListener { onErase() }
    }

    private fun button(): MaterialButton = MaterialButton(root.context).apply {
        layoutParams = buttonLayout()
        minWidth = 0
        minimumWidth = 0
        setPadding(0, 0, 0, 0)
        insetTop = 0
        insetBottom = 0
        cornerRadius = (32 * resources.displayMetrics.density).toInt()
        strokeWidth = resources.displayMetrics.density.toInt().coerceAtLeast(1)
        strokeColor = android.content.res.ColorStateList.valueOf(colors.outline)
        backgroundTintList = android.content.res.ColorStateList.valueOf(colors.background)
        rippleColor = android.content.res.ColorStateList.valueOf(ColorUtils.setAlphaComponent(colors.accent, 70))
        setTextColor(colors.foreground)
        textSize = 25f
    }

    private fun buttonLayout(): GridLayout.LayoutParams = GridLayout.LayoutParams().apply {
        val density = root.resources.displayMetrics.density
        val margin = (4 * density).toInt()
        width = 0
        height = (64 * density).toInt()
        rowSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.CENTER)
        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        setMargins(margin, margin, margin, margin)
    }
}
