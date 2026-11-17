package sudoku.domain.game

import sudoku.domain.common.OptionalValue

class GameTimer(initialMillis: Long = 0, private val now: () -> Long) {
    private var accumulatedMillis: Long = initialMillis
    private var startedAt: OptionalValue<Long> = OptionalValue.Absent

    init {
        require(initialMillis >= 0)
    }

    val elapsedMillis: Long
        get() = accumulatedMillis + startedAt.fold({ 0L }) { start: Long -> (now() - start).coerceAtLeast(0) }

    fun resume() {
        if (!startedAt.isPresent) startedAt = OptionalValue.Present(now())
    }

    fun pause() {
        accumulatedMillis = elapsedMillis
        startedAt = OptionalValue.Absent
    }
}
