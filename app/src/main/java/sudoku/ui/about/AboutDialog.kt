package sudoku.ui.about

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import sudoku.R

class AboutDialog(private val context: Context) {
    fun show() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_about, FrameLayout(context), false)
        val version = context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        view.findViewById<TextView>(R.id.about_version).text =
            context.getString(R.string.about_version, version.versionName)
        val dialog = MaterialAlertDialogBuilder(context).setView(view).create()
        view.findViewById<View>(R.id.about_close).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
