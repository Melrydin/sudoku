package sudoku.ui.game

import sudoku.domain.game.GameSession

sealed interface GamePreparationState {
    data object Loading : GamePreparationState
    data class Ready(val session: GameSession) : GamePreparationState
    data object Failed : GamePreparationState
}
