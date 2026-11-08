package com.example.sudoku.home

import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import com.example.sudoku.R

enum class HomeTheme(
    @param:StringRes val label: Int,
    @param:StyleRes val style: Int,
    val lightSystemBars: Boolean = false,
) {
    AURUM(R.string.theme_aurum, R.style.Theme_Sudoku),
    PAPER(R.string.theme_paper, R.style.Theme_Sudoku_Paper, true),
    OCEAN(R.string.theme_ocean, R.style.Theme_Sudoku_Ocean, true),
    ROSE(R.string.theme_rose, R.style.Theme_Sudoku_Rose, true),
    MIDNIGHT(R.string.theme_midnight, R.style.Theme_Sudoku_Midnight);

    fun shiftedBy(step: Int): HomeTheme = entries[Math.floorMod(ordinal + step, entries.size)]
}
