package sudoku.ui.game

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import sudoku.domain.common.OptionalValue
import sudoku.domain.game.GameSession
import sudoku.domain.game.NewGameFactory
import sudoku.domain.puzzle.PuzzleId

class GamePreparationModel : ViewModel() {
    private val worker: ExecutorService = Executors.newSingleThreadExecutor()
    private var task: OptionalValue<Future<*>> = OptionalValue.Absent
    private var started: Boolean = false
    private val mutableState: MutableLiveData<GamePreparationState> =
        MutableLiveData<GamePreparationState>(GamePreparationState.Loading)
    val state: LiveData<GamePreparationState> = mutableState

    fun prepare(saved: OptionalValue<GameSession>, puzzle: PuzzleId, prefillNotes: Boolean) {
        if (started) return
        started = true
        saved.ifPresent { game: GameSession -> setSession(game); return }
        task = OptionalValue.Present(worker.submit { generate(puzzle, prefillNotes) })
    }

    fun retry(puzzle: PuzzleId, prefillNotes: Boolean) {
        task.ifPresent { work: Future<*> -> work.cancel(true) }
        started = false
        mutableState.value = GamePreparationState.Loading
        prepare(OptionalValue.Absent, puzzle, prefillNotes)
    }

    fun setSession(session: GameSession) {
        mutableState.value = GamePreparationState.Ready(session)
    }

    private fun generate(puzzle: PuzzleId, prefillNotes: Boolean) {
        try {
            mutableState.postValue(
                GamePreparationState.Ready(
                    NewGameFactory.create(
                        puzzle,
                        prefillNotes
                    )
                )
            )
        } catch (error: Exception) {
            if (error is CancellationException) return
            Log.e("GamePreparation", "Puzzle generation failed for ${puzzle.key}.", error)
            mutableState.postValue(GamePreparationState.Failed)
        }
    }

    override fun onCleared() {
        task.ifPresent { work: Future<*> -> work.cancel(true) }
        worker.shutdownNow()
        super.onCleared()
    }
}
