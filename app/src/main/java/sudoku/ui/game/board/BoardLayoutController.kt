package sudoku.ui.game.board

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import sudoku.R
import sudoku.domain.settings.BoardLayoutOptions

class BoardLayoutController(root: View) {
    private val area: View = root.findViewById<View>(R.id.board_area)
    private val board: View = root.findViewById<View>(R.id.sudoku_board)
    private var options: BoardLayoutOptions = BoardLayoutOptions()

    init {
        area.addOnLayoutChangeListener {
                _, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
            if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) update()
        }
    }

    fun apply(options: BoardLayoutOptions) {
        this.options = options
        area.doOnLayout { update() }
    }

    private fun update() {
        val maxSize: Int = (600 * area.resources.displayMetrics.density).toInt()
        val size: Int = minOf((area.width * options.widthFraction).toInt(), area.height, maxSize)
        if (size <= 0) return
        board.updateLayoutParams<ConstraintLayout.LayoutParams> {
            width = size
            height = size
            horizontalBias = options.horizontalBias
            verticalBias = options.verticalBias
        }
    }
}
