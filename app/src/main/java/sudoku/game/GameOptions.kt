package sudoku.game

data class GameOptions(
    val highlightSameDigits: Boolean = true,
    val showRemainingCount: Boolean = true,
    val prefillNotes: Boolean = false,
    val autoRemoveNotes: Boolean = true,
    val hideCompletedDigits: Boolean = true,
    val boardAnimations: Boolean = true,
    val showTimer: Boolean = true,
    val circleGivens: Boolean = true,
    val doubleTapErase: Boolean = false,
    val holdToErase: Boolean = false,
)
