package sudoku.data.game

import android.content.Context
import androidx.core.content.edit
import sudoku.domain.game.GameSession
import sudoku.domain.game.GameRepository
import sudoku.domain.puzzle.GeneratorVersion
import sudoku.domain.puzzle.PuzzleId

class SharedPreferencesGameRepository(context: Context) : GameRepository {
    private val preferences = context.getSharedPreferences("games", Context.MODE_PRIVATE)

    override fun load(puzzle: PuzzleId): GameSession? {
        val encoded = preferences.getString(puzzle.key, null)
        val session = if (encoded != null) GameStateCodec.decode(encoded) else migrateLegacy(puzzle)
        if (session?.isGameOver == true) delete(puzzle)
        return session?.takeUnless { it.isGameOver }
    }

    override fun save(puzzle: PuzzleId, session: GameSession) {
        if (session.isGameOver) return delete(puzzle)
        preferences.edit {
            putString(puzzle.key, GameStateCodec.encode(session))
            if (puzzle.isLegacySlot()) remove(puzzle.difficulty.name)
        }
    }

    override fun delete(puzzle: PuzzleId) {
        preferences.edit(commit = true) {
            remove(puzzle.key)
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
