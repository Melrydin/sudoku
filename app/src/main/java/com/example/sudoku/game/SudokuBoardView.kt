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
    var onCellErased: (Int) -> Unit = {}
    private val colors = GameColors(context)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cells = List(SudokuBoard.CELL_COUNT) { createCell(it) }

    init {
        columnCount = SudokuBoard.SIZE
        rowCount = SudokuBoard.SIZE
        layoutDirection = LAYOUT_DIRECTION_LTR
        cells.forEach(::addView)
    }

    fun render(state: GameState, options: GameOptions = GameOptions()) {
        val conflicts = SudokuRules.conflicts(state.board)
        cells.forEach { it.render(state, conflicts, options) }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        for (line in 1 until SudokuBoard.SIZE) drawGridLine(canvas, line)
    }

    private fun createCell(index: Int): SudokuCellView = SudokuCellView(context, index).apply {
        tag = "cell_$index"
        layoutParams = LayoutParams(
            spec(index / SudokuBoard.SIZE, 1f), spec(index % SudokuBoard.SIZE, 1f),
        ).apply { width = 0; height = 0 }
        setOnClickListener { onCellSelected(index) }
        onErase = { onCellErased(index) }
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
        gridPaint.color = if (major) colors.accent else ColorUtils.setAlphaComponent(colors.outline, 110)
        gridPaint.strokeWidth = resources.displayMetrics.density * if (major) 1.4f else 0.5f
        if (major) {
            canvas.drawLine(line * width / 9f, 0f, line * width / 9f, height.toFloat(), gridPaint)
            canvas.drawLine(0f, line * height / 9f, width.toFloat(), line * height / 9f, gridPaint)
        } else drawMinorLines(canvas, line)
    }

    private fun drawMinorLines(canvas: Canvas, line: Int) {
        val cellWidth = width / 9f
        val cellHeight = height / 9f
        for (cell in 0 until SudokuBoard.SIZE) {
            canvas.drawLine(line * cellWidth, (cell + 0.2f) * cellHeight,
                line * cellWidth, (cell + 0.8f) * cellHeight, gridPaint)
            canvas.drawLine((cell + 0.2f) * cellWidth, line * cellHeight,
                (cell + 0.8f) * cellWidth, line * cellHeight, gridPaint)
        }
    }
}
