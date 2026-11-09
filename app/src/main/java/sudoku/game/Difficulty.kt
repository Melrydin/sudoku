package sudoku.game

enum class Difficulty(val id: String) {
    BEGINNER("beginner"),
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard"),
    EXTREME("expert");

    fun shiftedBy(step: Int): Difficulty = entries[Math.floorMod(ordinal + step, entries.size)]
}
