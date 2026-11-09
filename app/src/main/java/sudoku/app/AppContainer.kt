package sudoku.app

import android.content.Context
import sudoku.data.game.SharedPreferencesGameRepository
import sudoku.data.preferences.AppPreferences
import sudoku.data.preferences.GamePreferences
import sudoku.domain.game.GameRepository
import sudoku.data.statistics.SharedPreferencesBestTimeRepository
import sudoku.domain.statistics.BestTimeRepository

class AppContainer(context: Context) {
    val games: GameRepository = SharedPreferencesGameRepository(context.applicationContext)
    val bestTimes: BestTimeRepository = SharedPreferencesBestTimeRepository(context.applicationContext)
    val appPreferences = AppPreferences(context.applicationContext)
    val gamePreferences = GamePreferences(context.applicationContext)
}

internal val Context.appContainer: AppContainer
    get() = (applicationContext as SudokuApplication).container
