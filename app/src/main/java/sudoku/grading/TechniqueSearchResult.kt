package sudoku.grading

sealed interface TechniqueSearchResult {
    data class Found(val step: LogicalStep) : TechniqueSearchResult
    data object NotFound : TechniqueSearchResult
    data object NotImplemented : TechniqueSearchResult
}
