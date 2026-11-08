package com.example.sudoku.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.GridLayout
import androidx.core.view.ViewCompat
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.graphics.ColorUtils

class SudokuBoardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    GridLayout(context, attrs) {
    var onCellSelected: (Int) -> Unit = {}
    private val colors = GameColors(context)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cells = List(SudokuBoard.CELL_COUNT) { createCell(it) }

    init {
        columnCount = SudokuBoard.SIZE
        rowCount = SudokuBoard.SIZE
        layoutDirection = LAYOUT_DIRECTION_LTR
        cells.forEach(::addView)
    }

    fun render(state: GameState) {
        val conflicts = SudokuRules.conflicts(state.board)
        cells.forEach { it.render(state, conflicts) }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        for (line in 0..SudokuBoard.SIZE) drawGridLine(canvas, line)
    }

    private fun createCell(index: Int): SudokuCellView = SudokuCellView(context, index).apply {
        tag = "cell_$index"
        layoutParams = LayoutParams(
            spec(index / SudokuBoard.SIZE, 1f), spec(index % SudokuBoard.SIZE, 1f),
        ).apply { width = 0; height = 0 }
        setOnClickListener { onCellSelected(index) }
        ViewCompat.setAccessibilityDelegate(this, cellAccessibility(index))
    }

    private fun cellAccessibility(index: Int): AccessibilityDelegateCompat = object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(host: android.view.View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(
                index / SudokuBoard.SIZE, 1, index % SudokuBoard.SIZE, 1, false, host.isSelected,
            ))
        }
    }

    private fun drawGridLine(canvas: Canvas, line: Int) {
        val major = line % SudokuBoard.BOX_SIZE == 0
        gridPaint.color = if (major) colors.outline else ColorUtils.setAlphaComponent(colors.outline, 85)
        gridPaint.strokeWidth = resources.displayMetrics.density * if (major) 1.4f else 0.5f
        val offset = gridPaint.strokeWidth / 2
        val x = (line * width / 9f).coerceIn(offset, width - offset)
        val y = (line * height / 9f).coerceIn(offset, height - offset)
        canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
        canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
    }
}
