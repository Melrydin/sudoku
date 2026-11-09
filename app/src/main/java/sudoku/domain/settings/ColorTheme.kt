package sudoku.domain.settings

enum class ColorTheme {
    AURUM,
    PAPER,
    OCEAN,
    ROSE,
    MIDNIGHT;

    fun shiftedBy(step: Int): ColorTheme = entries[Math.floorMod(ordinal + step, entries.size)]
}
