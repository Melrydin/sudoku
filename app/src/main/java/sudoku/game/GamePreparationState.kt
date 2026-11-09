package sudoku.game

sealed interface GamePreparationState {
    data object Loading : GamePreparationState
    data class Ready(val session: GameSession) : GamePreparationState
    data object Failed : GamePreparationState
}
