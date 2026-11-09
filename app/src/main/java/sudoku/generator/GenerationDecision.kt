package sudoku.generator

internal data class GenerationDecision(
    val cell: Int,
    val remainingCandidates: List<Int>,
)
