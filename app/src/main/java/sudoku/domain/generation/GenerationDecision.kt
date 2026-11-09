package sudoku.domain.generation

internal data class GenerationDecision(
    val cell: Int,
    val remainingCandidates: List<Int>,
)
