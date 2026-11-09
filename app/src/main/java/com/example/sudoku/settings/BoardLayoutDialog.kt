package com.example.sudoku.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.example.sudoku.R
import com.example.sudoku.game.BoardLayoutOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider

class BoardLayoutDialog(
    context: Context,
    private val preferences: GamePreferences,
    private val onPreview: (BoardLayoutOptions) -> Unit,
    private val onClosed: () -> Unit,
) {
    private val root = LayoutInflater.from(context).inflate(R.layout.dialog_board_layout, FrameLayout(context), false)
    private val dialog = MaterialAlertDialogBuilder(context).setView(root).create()
    private var draft = preferences.boardLayout

    fun show(initial: BoardLayoutOptions = draft) {
        draft = initial
        bindSlider(R.id.layout_width, draft.widthFraction * 100) { copy(widthFraction = it) }
        bindSlider(R.id.layout_vertical, draft.verticalBias * 100) { copy(verticalBias = it) }
        bindSlider(R.id.layout_horizontal, draft.horizontalBias * 100) { copy(horizontalBias = it) }
        root.findViewById<View>(R.id.layout_reset_horizontal).setOnClickListener {
            root.findViewById<Slider>(R.id.layout_horizontal).value = 50f
        }
        root.findViewById<View>(R.id.layout_cancel).setOnClickListener { dialog.dismiss() }
        root.findViewById<View>(R.id.layout_apply).setOnClickListener {
            preferences.boardLayout = draft
            dialog.dismiss()
        }
        dialog.setOnDismissListener { onClosed() }
        dialog.show()
        onPreview(draft)
    }

    fun release() {
        dialog.setOnDismissListener(null)
        dialog.dismiss()
    }

    private fun bindSlider(id: Int, value: Float, update: BoardLayoutOptions.(Float) -> BoardLayoutOptions) {
        root.findViewById<Slider>(id).apply {
            this.value = value
            addOnChangeListener { _, next, _ ->
                draft = draft.update(next / 100f)
                onPreview(draft)
            }
        }
    }
}
