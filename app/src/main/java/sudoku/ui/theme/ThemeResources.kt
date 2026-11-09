package sudoku.ui.theme

import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import sudoku.R
import sudoku.domain.settings.ColorTheme

private data class ThemeResources(
    @param:StringRes val label: Int,
    @param:StyleRes val style: Int,
    val lightSystemBars: Boolean = false,
)

private val ColorTheme.resources: ThemeResources
    get() = when (this) {
        ColorTheme.AURUM -> ThemeResources(R.string.theme_aurum, R.style.Theme_Sudoku)
        ColorTheme.PAPER -> ThemeResources(R.string.theme_paper, R.style.Theme_Sudoku_Paper, true)
        ColorTheme.OCEAN -> ThemeResources(R.string.theme_ocean, R.style.Theme_Sudoku_Ocean, true)
        ColorTheme.ROSE -> ThemeResources(R.string.theme_rose, R.style.Theme_Sudoku_Rose, true)
        ColorTheme.MIDNIGHT -> ThemeResources(R.string.theme_midnight, R.style.Theme_Sudoku_Midnight)
    }

val ColorTheme.label: Int
    @StringRes get() = resources.label

val ColorTheme.style: Int
    @StyleRes get() = resources.style

val ColorTheme.lightSystemBars: Boolean get() = resources.lightSystemBars
