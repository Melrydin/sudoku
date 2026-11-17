package sudoku.data.statistics

import org.json.JSONObject
import sudoku.domain.common.OptionalValue
import sudoku.domain.puzzle.Difficulty
import sudoku.domain.puzzle.GeneratorVersion
import sudoku.domain.puzzle.PuzzleId
import sudoku.domain.statistics.GameResult

internal object GameResultCodec {
    fun encode(result: GameResult): String = JSONObject()
        .put("version", 1)
        .put("gameId", result.gameId)
        .put("difficulty", result.puzzle.difficulty.id)
        .put("level", result.puzzle.level)
        .put("generatorVersion", result.puzzle.version.number)
        .put("elapsedMillis", result.elapsedMillis)
        .put("assistancePoints", result.assistancePoints)
        .put("finishedAt", result.finishedAt)
        .toString()

    fun decode(encoded: String): OptionalValue<GameResult> = runCatching {
        val json: JSONObject = JSONObject(encoded)
        require(json.getInt("version") == 1)
        val difficulty: Difficulty = Difficulty.entries.first { it.id == json.getString("difficulty") }
        val version: GeneratorVersion =
            GeneratorVersion.entries.first { it.number == json.getInt("generatorVersion") }
        GameResult(
            json.getString("gameId"),
            PuzzleId(difficulty, json.getInt("level"), version),
            json.getLong("elapsedMillis"),
            json.getInt("assistancePoints"),
            json.getLong("finishedAt")
        )
    }.fold({ result: GameResult -> OptionalValue.Present(result) }, { OptionalValue.Absent })
}
