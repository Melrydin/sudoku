package com.example.sudoku.settings

import androidx.annotation.StringRes
import com.example.sudoku.R

enum class SettingsSection(@param:StringRes val title: Int) {
    MENU(R.string.settings),
    ASSISTS(R.string.settings_assists),
    GAMEPLAY(R.string.settings_gameplay_title),
}
