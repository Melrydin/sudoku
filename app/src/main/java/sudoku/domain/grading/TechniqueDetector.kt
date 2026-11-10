package sudoku.domain.grading

interface TechniqueDetector {
    val technique: SolvingTechnique

    // Return the first applicable step without modifying the board or candidate sets.
    fun findStep(state: LogicalState): TechniqueSearchResult
}
