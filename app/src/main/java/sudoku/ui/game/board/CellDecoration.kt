package sudoku.ui.game.board

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.ColorUtils
import sudoku.domain.common.OptionalValue
import sudoku.domain.game.GameState
import sudoku.domain.settings.GameOptions
import sudoku.ui.theme.ThemeColors

class CellDecoration(private val colors: ThemeColors, private val invalidate: () -> Unit) {
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var color: OptionalValue<Int> = OptionalValue.Absent
    private var selected: Boolean = false
    private var scale: Float = 1f
    private var animator: OptionalValue<ValueAnimator> = OptionalValue.Absent
    val isCircled: Boolean get() = color.isPresent
    val foreground: Int
        get() {
            val background: Int = color.getOrElse { return colors.foreground }
            return listOf(
                colors.foreground,
                colors.background
            ).maxBy { ColorUtils.calculateContrast(it, background) }
        }

    fun update(state: GameState, index: Int, options: GameOptions) {
        val wasSelected: Boolean = selected
        selected = state.selectedCell == index
        color = circleColor(state, index, options)
        if (selected && !wasSelected && options.boardAnimations) animateSelection()
        else if (!options.boardAnimations) cancelAnimation()
    }

    fun draw(canvas: Canvas, width: Int, height: Int) {
        paint.color = color.getOrElse { return }
        canvas.drawCircle(width / 2f, height / 2f, minOf(width, height) * 0.43f * scale, paint)
    }

    fun cancelAnimation() {
        animator.ifPresent { animation: ValueAnimator -> animation.cancel() }
        animator = OptionalValue.Absent
        scale = 1f
    }

    private fun circleColor(state: GameState, index: Int, options: GameOptions): OptionalValue<Int> {
        val digit: Int = state.board.cells.getOrNull(state.selectedCell) ?: 0
        return when {
            selected -> OptionalValue.Present(colors.accent)
            options.highlightSameDigits && digit != 0 && state.board[index] == digit ->
                OptionalValue.Present(colors.accent)
            options.circleGivens && state.puzzle[index] != 0 ->
                OptionalValue.Present(ColorUtils.blendARGB(colors.background, colors.foreground, 0.35f))

            else -> OptionalValue.Absent
        }
    }

    private fun animateSelection() {
        animator.ifPresent { animation: ValueAnimator -> animation.cancel() }
        animator = OptionalValue.Present(ValueAnimator.ofFloat(0.85f, 1f).apply {
            duration = 140
            addUpdateListener { scale = it.animatedValue as Float; invalidate() }
            start()
        })
    }
}
