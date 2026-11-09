package sudoku.app

import android.app.Application

class SudokuApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
