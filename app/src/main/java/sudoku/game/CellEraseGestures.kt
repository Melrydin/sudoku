package sudoku.game

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

class CellEraseGestures(context: Context, private val erase: () -> Unit) {
    var options = GameOptions()
    private val detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean = true

        override fun onDoubleTap(event: MotionEvent): Boolean {
            if (options.doubleTapErase) erase()
            return options.doubleTapErase
        }
    })

    fun onTouch(event: MotionEvent) {
        detector.onTouchEvent(event)
    }

    fun onHold(): Boolean {
        if (options.holdToErase) erase()
        return options.holdToErase
    }
}
