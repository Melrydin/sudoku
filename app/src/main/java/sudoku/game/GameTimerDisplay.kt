package sudoku.game

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import sudoku.R

class GameTimerDisplay(
    private val view: TextView,
    private val timer: GameTimer,
    private val isComplete: () -> Boolean,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var active = false
    private val tick = object : Runnable {
        override fun run() {
            refresh()
            if (active) handler.postDelayed(this, 1_000)
        }
    }

    fun start() {
        if (active) return
        active = true
        tick.run()
    }

    fun stop() {
        active = false
        handler.removeCallbacks(tick)
        refresh()
    }

    fun refresh() {
        if (active && !isComplete()) timer.resume() else timer.pause()
        val time = PlayTimeFormatter.format(timer.elapsedMillis, view.resources.configuration.locales[0])
        view.text = time
        view.contentDescription = view.context.getString(R.string.play_time_description, time)
    }
}
