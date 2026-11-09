package sudoku.ui.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import sudoku.R
import sudoku.app.appContainer
import sudoku.domain.game.GameSession
import sudoku.domain.game.GameTimer
import sudoku.domain.game.NewGameFactory
import sudoku.domain.generation.GeneratorConfig
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.GeneratorVersion
import sudoku.domain.puzzle.PuzzleId
import sudoku.ui.theme.ThemePalette
import sudoku.ui.common.ThemedActivity
import sudoku.ui.game.board.BoardLayoutController
import sudoku.ui.game.result.GameOverDialog
import sudoku.ui.game.result.VictoryActivity
import sudoku.ui.navigation.HomeNavigation
import sudoku.domain.statistics.GameResult
import sudoku.ui.settings.SettingsActivity

class GameActivity : ThemedActivity() {
    private val store by lazy { appContainer.games }
    private val gamePreferences by lazy { appContainer.gamePreferences }
    private val puzzle by lazy { puzzleFromIntent() }
    private val preparation by lazy { ViewModelProvider(this)[GamePreparationModel::class.java] }
    private var startFresh = false
    private var gameReady = false
    private var gameOverPending = false
    private var victoryOpened = false
    private var gameOverDialog: GameOverDialog? = null
    private lateinit var session: GameSession
    private lateinit var screen: GameScreen
    private lateinit var timer: GameTimer
    private lateinit var timerDisplay: GameTimerDisplay
    private lateinit var boardLayout: BoardLayoutController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.getBoolean(EXTRA_GAME_OVER) == true) {
            showGameOver()
            return
        }
        preparation.state.observe(this, ::renderPreparation)
        prepareSession(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (refreshTheme()) return
        if (gameOverPending) {
            showGameOver()
            return
        }
        if (gameReady) resumeGame()
    }

    private fun resumeGame() {
        session.recordAssistance(gamePreferences.options)
        screen.render()
        if (session.state.isComplete) { showVictory(); return }
        boardLayout.apply(gamePreferences.boardLayout)
        timerDisplay.start()
    }

    override fun onPause() {
        if (::timerDisplay.isInitialized) timerDisplay.stop()
        if (::session.isInitialized) saveSession()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_NEW_GAME, startFresh)
        outState.putBoolean(EXTRA_GAME_OVER, gameOverPending)
        if (gameReady) saveSession()
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        gameOverDialog?.dismiss()
        super.onDestroy()
    }

    private fun prepareSession(savedInstanceState: Bundle?) {
        startFresh = savedInstanceState?.getBoolean(EXTRA_NEW_GAME)
            ?: intent.getBooleanExtra(EXTRA_NEW_GAME, false)
        val saved = if (startFresh) null else store.load(puzzle)
        preparation.prepare(saved, puzzle, gamePreferences.options.prefillNotes)
    }

    private fun renderPreparation(state: GamePreparationState) {
        when (state) {
            GamePreparationState.Loading -> showLoading(false)
            GamePreparationState.Failed -> showLoading(true)
            is GamePreparationState.Ready -> bindSession(state.session)
        }
    }

    private fun showLoading(failed: Boolean) {
        gameReady = false
        setContentView(R.layout.activity_game_loading)
        applySystemInsets(findViewById(R.id.game_loading_root))
        findViewById<View>(R.id.back_home).setOnClickListener { finish() }
        findViewById<View>(R.id.generation_progress).isVisible = !failed
        findViewById<View>(R.id.retry_generation).apply {
            isVisible = failed
            setOnClickListener { preparation.retry(puzzle, gamePreferences.options.prefillNotes) }
        }
        findViewById<TextView>(R.id.game_preparation_message).setText(
            if (failed) R.string.generation_failed else R.string.generating_puzzle,
        )
    }

    private fun bindSession(restored: GameSession) {
        if (::timerDisplay.isInitialized) timerDisplay.stop()
        session = restored
        session.recordAssistance(gamePreferences.options)
        timer = GameTimer(session.elapsedMillis, SystemClock::elapsedRealtime)
        setContentView(R.layout.activity_game)
        bindGameViews()
        startFresh = false
        intent.removeExtra(EXTRA_NEW_GAME)
        saveSession()
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) resumeGame()
    }

    private fun bindGameViews() {
        val root = findViewById<View>(R.id.game_root)
        applySystemInsets(root)
        timerDisplay = GameTimerDisplay(findViewById(R.id.game_timer), timer) {
            session.state.isComplete || session.isGameOver
        }
        screen = GameScreen(root, session, ::gameChanged) { gamePreferences.options }
        screen.bind(puzzle, ::finish, ::chooseTheme, ::confirmRestart, ::openSettings)
        boardLayout = BoardLayoutController(root)
        gameReady = true
    }

    private fun saveSession() {
        if (session.isGameOver) return store.delete(puzzle)
        session.trackElapsedTime(timer.elapsedMillis)
        store.save(puzzle, session)
    }

    private fun gameChanged() {
        session.recordAssistance(gamePreferences.options)
        timerDisplay.refresh()
        saveSession()
        if (session.isGameOver) showGameOver()
        else if (session.state.isComplete) showVictory()
    }

    private fun showGameOver() {
        gameOverPending = true
        if (::timerDisplay.isInitialized) timerDisplay.stop()
        store.delete(puzzle)
        if (!gameReady) showGameOverBackground()
        val dialog = gameOverDialog ?: GameOverDialog(this, ::returnHome).also { gameOverDialog = it }
        dialog.show()
    }

    private fun showGameOverBackground() {
        setContentView(R.layout.activity_game_loading)
        applySystemInsets(findViewById(R.id.game_loading_root))
        findViewById<View>(R.id.generation_progress).isVisible = false
        findViewById<TextView>(R.id.game_preparation_message).setText(R.string.game_over)
        findViewById<View>(R.id.back_home).setOnClickListener { returnHome() }
    }

    private fun returnHome() {
        HomeNavigation.open(this, puzzle)
    }

    private fun showVictory() {
        if (victoryOpened || isFinishing) return
        victoryOpened = true
        timerDisplay.stop()
        saveSession()
        val result = GameResult(session.id, puzzle, session.elapsedMillis,
            session.assistancePoints, System.currentTimeMillis())
        appContainer.bestTimes.record(result)
        startActivity(VictoryActivity.intent(this, session.id))
        finish()
    }

    private fun chooseTheme() {
        ThemePalette(this, preferences.theme, ::selectTheme).show(findViewById(R.id.game_theme))
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun confirmRestart() {
        if (!session.state.hasEntries || session.state.isComplete) restart() else MaterialAlertDialogBuilder(this)
            .setTitle(R.string.restart_title)
            .setMessage(R.string.restart_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.new_game) { _, _ -> restart() }
            .show()
    }

    private fun restart() {
        timerDisplay.stop()
        val next = NewGameFactory.fromPuzzle(
            session.state.puzzle, gamePreferences.options.prefillNotes, session.solution,
        )
        preparation.setSession(next)
    }

    private fun puzzleFromIntent(): PuzzleId {
        val difficulty = Difficulty.entries.find { it.name == intent.getStringExtra(EXTRA_DIFFICULTY) }
            ?: Difficulty.EASY
        val version = GeneratorVersion.entries.find { it.number == intent.getIntExtra(EXTRA_VERSION, 1) }
            ?: GeneratorConfig.VERSION
        return PuzzleId(difficulty, intent.getIntExtra(EXTRA_LEVEL, 1).coerceAtLeast(1), version)
    }

    companion object {
        private const val EXTRA_DIFFICULTY = "difficulty"
        private const val EXTRA_LEVEL = "level"
        private const val EXTRA_VERSION = "generator_version"
        private const val EXTRA_NEW_GAME = "new_game"
        private const val EXTRA_GAME_OVER = "game_over"

        fun intent(context: Context, puzzle: PuzzleId, newGame: Boolean): Intent =
            Intent(context, GameActivity::class.java)
                .putExtra(EXTRA_DIFFICULTY, puzzle.difficulty.name)
                .putExtra(EXTRA_LEVEL, puzzle.level)
                .putExtra(EXTRA_VERSION, puzzle.version.number)
                .putExtra(EXTRA_NEW_GAME, newGame)
    }
}
