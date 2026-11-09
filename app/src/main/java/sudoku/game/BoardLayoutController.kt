package sudoku.game

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import sudoku.R

class BoardLayoutController(root: View) {
    private val area = root.findViewById<View>(R.id.board_area)
    private val board = root.findViewById<View>(R.id.sudoku_board)
    private var options = BoardLayoutOptions()

    init {
        area.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) update()
        }
    }

    fun apply(options: BoardLayoutOptions) {
        this.options = options
        area.doOnLayout { update() }
    }

    private fun update() {
        val maxSize = (600 * area.resources.displayMetrics.density).toInt()
        val size = minOf((area.width * options.widthFraction).toInt(), area.height, maxSize)
        if (size <= 0) return
        board.updateLayoutParams<ConstraintLayout.LayoutParams> {
            width = size
            height = size
            horizontalBias = options.horizontalBias
            verticalBias = options.verticalBias
        }
    }
}
