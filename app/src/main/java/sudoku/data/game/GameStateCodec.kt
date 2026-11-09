package sudoku.data.game

import org.json.JSONArray
import org.json.JSONObject
import sudoku.domain.board.SudokuBoard
import sudoku.domain.game.GameSession
import sudoku.domain.game.GameState
import sudoku.domain.game.GameAssist

object GameStateCodec {
    fun encode(session: GameSession): String = JSONObject()
        .put("version", 1)
        .put("elapsedMillis", session.elapsedMillis)
        .put("solution", session.solution.cells.joinToString(""))
        .put("mistakes", session.mistakes)
        .put("id", session.id)
        .put("assistance", JSONArray(session.usedAssistance.map { it.name }))
        .put("state", encodeState(session.state))
        .put("history", JSONArray().apply { session.undoHistory.forEach { put(encodeState(it)) } })
        .toString()

    fun decode(value: String): GameSession? = runCatching {
        val json = JSONObject(value)
        require(json.getInt("version") == 1)
        val state = decodeState(json.getJSONObject("state"))
        val history = json.getJSONArray("history")
        require(history.length() <= GameSession.MAX_HISTORY)
        val snapshots = (0 until history.length()).map { decodeState(history.getJSONObject(it)) }
        require(snapshots.all { it.puzzle.cells == state.puzzle.cells })
        restoreSession(json, state, snapshots)
    }.getOrNull()

    private fun restoreSession(json: JSONObject, state: GameState, history: List<GameState>): GameSession = GameSession(
        initialState = state,
        history = history,
        initialElapsedMillis = json.optLong("elapsedMillis", 0),
        initialSolution = decodeSolution(json),
        initialMistakes = decodeMistakes(json),
        initialAssistance = decodeAssistance(json),
        id = if (json.has("id")) json.getString("id") else java.util.UUID.randomUUID().toString(),
    )

    private fun decodeAssistance(json: JSONObject): Set<GameAssist> {
        val values = if (json.has("assistance")) json.getJSONArray("assistance") else return emptySet()
        require(values.length() <= GameAssist.entries.size)
        return (0 until values.length()).map { GameAssist.valueOf(values.getString(it)) }.toSet()
    }

    private fun decodeSolution(json: JSONObject): SudokuBoard? =
        if (json.has("solution")) SudokuBoard.parse(json.getString("solution")) else null

    private fun decodeMistakes(json: JSONObject): Int = if (json.has("mistakes")) json.getInt("mistakes") else 0

    private fun encodeState(state: GameState): JSONObject = JSONObject()
        .put("puzzle", state.puzzle.cells.joinToString(""))
        .put("board", state.board.cells.joinToString(""))
        .put("selected", state.selectedCell)
        .put("noteMode", state.noteMode)
        .put("notes", JSONArray().apply { state.notes.forEach { put(it.sorted().joinToString("")) } })

    private fun decodeState(json: JSONObject): GameState {
        val notes = json.getJSONArray("notes")
        require(notes.length() == SudokuBoard.CELL_COUNT)
        return GameState(
            puzzle = SudokuBoard.parse(json.getString("puzzle")),
            board = SudokuBoard.parse(json.getString("board")),
            notes = (0 until notes.length()).map { index -> parseNotes(notes.getString(index)) },
            selectedCell = json.getInt("selected"),
            noteMode = json.getBoolean("noteMode"),
        )
    }

    private fun parseNotes(value: String): Set<Int> {
        require(value.all { it in '1'..'9' })
        return value.map { it.digitToInt() }.toSet()
    }
}
