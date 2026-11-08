package com.example.sudoku.game

class GameTimer(initialMillis: Long = 0, private val now: () -> Long) {
    private var accumulatedMillis: Long = initialMillis
    private var startedAt: Long? = null

    init {
        require(initialMillis >= 0)
    }

    val elapsedMillis: Long
        get() = accumulatedMillis + (startedAt?.let { (now() - it).coerceAtLeast(0) } ?: 0)

    fun resume() {
        if (startedAt == null) startedAt = now()
    }

    fun pause() {
        accumulatedMillis = elapsedMillis
        startedAt = null
    }
}
