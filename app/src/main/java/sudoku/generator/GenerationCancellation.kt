package sudoku.generator

import java.util.concurrent.CancellationException

internal fun checkGenerationCancellation() {
    if (Thread.currentThread().isInterrupted) throw CancellationException("Puzzle generation was cancelled.")
}
