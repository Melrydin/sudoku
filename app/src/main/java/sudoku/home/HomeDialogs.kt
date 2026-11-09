package sudoku.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import sudoku.R
import sudoku.ui.ThemePalette
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeDialogs(private val context: Context) {
    fun showThemes(anchor: View, selected: HomeTheme, onSelected: (HomeTheme) -> Unit) {
        ThemePalette(context, selected, onSelected).show(anchor)
    }

    fun showBestTimes() {
        context.startActivity(Intent(context, TimesActivity::class.java))
    }

    fun showAbout() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_about, FrameLayout(context), false)
        val version = context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        view.findViewById<TextView>(R.id.about_version).text =
            context.getString(R.string.about_version, version.versionName)
        val dialog = MaterialAlertDialogBuilder(context).setView(view).create()
        view.findViewById<View>(R.id.about_close).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
