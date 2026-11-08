package com.example.sudoku.game

import android.widget.GridLayout
import androidx.core.graphics.ColorUtils
import com.example.sudoku.R
import com.example.sudoku.ui.formatNumber
import com.google.android.material.button.MaterialButton

class NumberPad(private val root: GridLayout, private val onNumber: (Int) -> Unit) {
    private val colors = GameColors(root.context)

    fun bind(onErase: () -> Unit) {
        for (number in 1..9) root.addView(numberButton(number))
        root.addView(eraseButton(onErase))
    }

    private fun numberButton(number: Int): MaterialButton = button().apply {
        text = context.formatNumber(number)
        tag = "number_$number"
        contentDescription = context.getString(R.string.enter_number, text)
        setOnClickListener { onNumber(number) }
    }

    private fun eraseButton(onErase: () -> Unit): MaterialButton = button().apply {
        id = R.id.erase
        icon = androidx.appcompat.content.res.AppCompatResources.getDrawable(context, R.drawable.ic_erase)
        iconTint = android.content.res.ColorStateList.valueOf(colors.foreground)
        iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
        iconPadding = 0
        contentDescription = context.getString(R.string.erase)
        setOnClickListener { onErase() }
    }

    private fun button(): MaterialButton = MaterialButton(root.context).apply {
        layoutParams = buttonLayout()
        minWidth = 0
        minimumWidth = 0
        setPadding(0, 0, 0, 0)
        insetTop = 6
        insetBottom = 6
        cornerRadius = (32 * resources.displayMetrics.density).toInt()
        strokeWidth = resources.displayMetrics.density.toInt().coerceAtLeast(1)
        strokeColor = android.content.res.ColorStateList.valueOf(colors.outline)
        backgroundTintList = android.content.res.ColorStateList.valueOf(colors.background)
        rippleColor = android.content.res.ColorStateList.valueOf(ColorUtils.setAlphaComponent(colors.accent, 70))
        setTextColor(colors.foreground)
        textSize = 25f
    }

    private fun buttonLayout(): GridLayout.LayoutParams = GridLayout.LayoutParams().apply {
        val density = root.resources.displayMetrics.density
        val margin = (4 * density).toInt()
        width = 0
        height = (64 * density).toInt()
        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        setMargins(margin, margin, margin, margin)
    }
}
