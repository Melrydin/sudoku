package com.example.sudoku.game

import android.content.Context
import androidx.core.content.edit
import com.example.sudoku.home.Difficulty

class GameStore(context: Context) {
    private val preferences = context.getSharedPreferences("games", Context.MODE_PRIVATE)

    fun load(difficulty: Difficulty): GameSession? =
        preferences.getString(difficulty.name, null)?.let(GameStateCodec::decode)

    fun save(difficulty: Difficulty, session: GameSession) {
        preferences.edit { putString(difficulty.name, GameStateCodec.encode(session)) }
    }
}
