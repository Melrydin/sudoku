package sudoku.data.game

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import sudoku.domain.common.OptionalValue
import sudoku.domain.game.GameRepository
import sudoku.domain.game.GameSession
import sudoku.domain.puzzle.GeneratorVersion
import sudoku.domain.puzzle.PuzzleId

class SharedPreferencesGameRepository(context: Context) : GameRepository {
    private val preferences: SharedPreferences = context.getSharedPreferences("games", Context.MODE_PRIVATE)

    override fun load(puzzle: PuzzleId): OptionalValue<GameSession> {
        val encoded: OptionalValue<String> = OptionalValue.fromNullable(preferences.getString(puzzle.key, null))
        val session: OptionalValue<GameSession> = encoded.fold({ migrateLegacy(puzzle) }, GameStateCodec::decode)
        if (session.matches { game: GameSession -> game.isGameOver }) delete(puzzle)
        return session.filter { game: GameSession -> !game.isGameOver }
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

    private fun migrateLegacy(puzzle: PuzzleId): OptionalValue<GameSession> {
        if (!puzzle.isLegacySlot()) return OptionalValue.Absent
        val encoded: String = preferences.getString(puzzle.difficulty.name, null) ?: return OptionalValue.Absent
        val session: OptionalValue<GameSession> = GameStateCodec.decode(encoded)
        session.ifPresent { game: GameSession -> save(puzzle, game) }
        return session
    }

    private fun PuzzleId.isLegacySlot(): Boolean = version == GeneratorVersion.V1 && level == 1
}
