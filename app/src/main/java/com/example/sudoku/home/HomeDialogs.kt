package com.example.sudoku.home

import android.content.Context
import androidx.annotation.StringRes
import com.example.sudoku.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeDialogs(private val context: Context) {
    fun showThemes(selected: HomeTheme, onSelected: (HomeTheme) -> Unit) {
        val labels = HomeTheme.entries.map { context.getString(it.label) }.toTypedArray()
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.choose_theme)
            .setSingleChoiceItems(labels, selected.ordinal) { dialog, index ->
                dialog.dismiss()
                onSelected(HomeTheme.entries[index])
            }
            .setNegativeButton(R.string.close, null)
            .show()
    }

    fun showBestTimes() {
        showMessage(R.string.best_times, R.string.no_best_times)
    }

    fun showAbout() {
        showMessage(R.string.about, R.string.about_message)
    }

    private fun showMessage(@StringRes title: Int, @StringRes message: Int) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.close, null)
            .show()
    }
}
