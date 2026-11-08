package com.example.sudoku.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import com.example.sudoku.R
import com.example.sudoku.home.Difficulty
import com.example.sudoku.home.HomeDialogs
import com.example.sudoku.ui.ThemedActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GameActivity : ThemedActivity() {
    private val store by lazy { GameStore(this) }
    private val difficulty by lazy {
        Difficulty.entries.find { it.name == intent.getStringExtra(EXTRA_DIFFICULTY) } ?: Difficulty.EASY
    }
    private lateinit var session: GameSession
    private lateinit var screen: GameScreen
    private lateinit var timer: GameTimer
    private lateinit var timerDisplay: GameTimerDisplay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreSession(savedInstanceState)
        setContentView(R.layout.activity_game)
        val root = findViewById<android.view.View>(R.id.game_root)
        applySystemInsets(root)
        timerDisplay = GameTimerDisplay(findViewById(R.id.game_timer), timer) { session.state.isComplete }
        screen = GameScreen(root, session, ::gameChanged)
        screen.bind(difficulty, ::finish, ::chooseTheme, ::confirmRestart)
    }

    override fun onResume() {
        super.onResume()
        timerDisplay.start()
    }

    override fun onPause() {
        if (::timerDisplay.isInitialized) timerDisplay.stop()
        if (::session.isInitialized) saveSession()
        super.onPause()
    }

    private fun restoreSession(savedInstanceState: Bundle?) {
        val startFresh = savedInstanceState == null && intent.getBooleanExtra(EXTRA_NEW_GAME, false)
        session = if (startFresh) GameSession(SamplePuzzle.create())
        else store.load(difficulty) ?: GameSession(SamplePuzzle.create())
        intent.removeExtra(EXTRA_NEW_GAME)
        timer = GameTimer(session.elapsedMillis, SystemClock::elapsedRealtime)
        saveSession()
    }

    private fun saveSession() {
        session.trackElapsedTime(timer.elapsedMillis)
        store.save(difficulty, session)
    }

    private fun gameChanged() {
        timerDisplay.refresh()
        saveSession()
    }

    private fun chooseTheme() {
        HomeDialogs(this).showThemes(preferences.theme, ::selectTheme)
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
        session = GameSession(SamplePuzzle.create())
        timer = GameTimer(now = SystemClock::elapsedRealtime)
        saveSession()
        recreate()
    }

    companion object {
        private const val EXTRA_DIFFICULTY = "difficulty"
        private const val EXTRA_NEW_GAME = "new_game"

        fun intent(context: Context, difficulty: Difficulty, newGame: Boolean): Intent =
            Intent(context, GameActivity::class.java)
                .putExtra(EXTRA_DIFFICULTY, difficulty.name)
                .putExtra(EXTRA_NEW_GAME, newGame)
    }
}
