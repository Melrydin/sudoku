package sudoku.game

import android.content.Context
import androidx.core.content.edit
import sudoku.generator.GeneratorVersion

class GameStore(context: Context) {
    private val preferences = context.getSharedPreferences("games", Context.MODE_PRIVATE)

    fun load(puzzle: PuzzleId): GameSession? {
        val encoded = preferences.getString(puzzle.key, null)
        return if (encoded != null) GameStateCodec.decode(encoded) else migrateLegacy(puzzle)
    }

    fun save(puzzle: PuzzleId, session: GameSession) {
        preferences.edit {
            putString(puzzle.key, GameStateCodec.encode(session))
            if (puzzle.isLegacySlot()) remove(puzzle.difficulty.name)
        }
    }

    private fun migrateLegacy(puzzle: PuzzleId): GameSession? {
        if (!puzzle.isLegacySlot()) return null
        val encoded = preferences.getString(puzzle.difficulty.name, null) ?: return null
        val session = GameStateCodec.decode(encoded) ?: return null
        save(puzzle, session)
        return session
    }

    private fun PuzzleId.isLegacySlot(): Boolean = version == GeneratorVersion.V1 && level == 1
}
