package sudoku.domain.common

import java.util.concurrent.CancellationException

internal fun checkCancellation() {
    if (Thread.currentThread().isInterrupted) throw CancellationException("Puzzle generation was cancelled.")
}
