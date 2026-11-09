package sudoku.grading

interface TechniqueDetector {
    val technique: SolvingTechnique

    fun findStep(state: LogicalState): TechniqueSearchResult
}
