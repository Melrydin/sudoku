package sudoku.domain.settings

data class BoardLayoutOptions(
    val widthFraction: Float = 1f,
    val verticalBias: Float = 0.5f,
    val horizontalBias: Float = 0.5f,
) {
    init {
        require(widthFraction in 0.6f..1f)
        require(verticalBias in 0f..1f && horizontalBias in 0f..1f)
    }
}
