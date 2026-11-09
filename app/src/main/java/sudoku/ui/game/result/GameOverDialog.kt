package sudoku.ui.game.result

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import sudoku.R

class GameOverDialog(context: Context, onHome: () -> Unit) {
    private val dialog = MaterialAlertDialogBuilder(context)
        .setTitle(R.string.game_over)
        .setMessage(R.string.game_over_message)
        .setCancelable(false)
        .setPositiveButton(R.string.back_home) { _, _ -> onHome() }
        .create()

    fun show() {
        if (!dialog.isShowing) dialog.show()
    }

    fun dismiss() = dialog.dismiss()
}
