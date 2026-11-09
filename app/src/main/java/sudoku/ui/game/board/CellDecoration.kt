package sudoku.ui.game.board

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.ColorUtils
import sudoku.domain.game.GameState
import sudoku.domain.settings.GameOptions
import sudoku.ui.theme.ThemeColors

class CellDecoration(private val colors: ThemeColors, private val invalidate: () -> Unit) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var color: Int? = null
    private var selected = false
    private var scale = 1f
    private var animator: ValueAnimator? = null
    val isCircled: Boolean get() = color != null
    val foreground: Int
        get() {
            val background = color ?: return colors.foreground
            return listOf(colors.foreground, colors.background).maxBy { ColorUtils.calculateContrast(it, background) }
        }

    fun update(state: GameState, index: Int, options: GameOptions) {
        val wasSelected = selected
        selected = state.selectedCell == index
        color = circleColor(state, index, options)
        if (selected && !wasSelected && options.boardAnimations) animateSelection()
        else if (!options.boardAnimations) cancelAnimation()
    }

    fun draw(canvas: Canvas, width: Int, height: Int) {
        paint.color = color ?: return
        canvas.drawCircle(width / 2f, height / 2f, minOf(width, height) * 0.43f * scale, paint)
    }

    fun cancelAnimation() {
        animator?.cancel()
        animator = null
        scale = 1f
    }

    private fun circleColor(state: GameState, index: Int, options: GameOptions): Int? {
        val digit = state.board.cells.getOrNull(state.selectedCell) ?: 0
        return when {
            selected -> colors.accent
            options.highlightSameDigits && digit != 0 && state.board[index] == digit -> colors.accent
            options.circleGivens && state.puzzle[index] != 0 ->
                ColorUtils.blendARGB(colors.background, colors.foreground, 0.35f)
            else -> null
        }
    }

    private fun animateSelection() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0.85f, 1f).apply {
            duration = 140
            addUpdateListener { scale = it.animatedValue as Float; invalidate() }
            start()
        }
    }
}
