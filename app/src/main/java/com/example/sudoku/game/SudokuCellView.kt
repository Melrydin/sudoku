package com.example.sudoku.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.ColorUtils
import com.example.sudoku.R
import com.example.sudoku.ui.formatNumber

class SudokuCellView @JvmOverloads constructor(context: Context, private val index: Int = 0) :
    AppCompatTextView(context) {
    private val colors = GameColors(context)
    private val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private var notes: Set<Int> = emptySet()

    init {
        gravity = Gravity.CENTER
        isFocusable = true
        setPadding(0, 0, 0, 0)
        setTextColor(colors.foreground)
    }

    fun render(state: GameState, conflicts: Set<Int>) {
        notes = state.notes[index]
        val given = state.puzzle[index] != 0
        text = state.board[index].takeIf { it != 0 }?.let { context.formatNumber(it) }.orEmpty()
        typeface = if (given) Typeface.DEFAULT_BOLD else Typeface.create("sans-serif-light", Typeface.NORMAL)
        setTextColor(if (index in conflicts) colors.error else colors.foreground)
        paintFlags = if (index in conflicts) paintFlags or Paint.UNDERLINE_TEXT_FLAG
        else paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        isSelected = state.selectedCell == index
        setBackgroundColor(backgroundColor(state))
        contentDescription = description(state, given, index in conflicts)
        invalidate()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, minOf(width, height) * 0.53f)
        notePaint.textSize = minOf(width, height) * 0.23f
        notePaint.color = colors.muted
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        notes.forEach { value -> drawNote(canvas, value) }
    }

    private fun drawNote(canvas: Canvas, value: Int) {
        val column = (value - 1) % SudokuBoard.BOX_SIZE
        val row = (value - 1) / SudokuBoard.BOX_SIZE
        val x = width * (column + 0.5f) / SudokuBoard.BOX_SIZE
        val y = height * (row + 0.5f) / SudokuBoard.BOX_SIZE
        canvas.drawText(context.formatNumber(value), x, y - (notePaint.ascent() + notePaint.descent()) / 2, notePaint)
    }

    private fun backgroundColor(state: GameState): Int {
        val selected = state.selectedCell
        return when {
            selected == index -> ColorUtils.blendARGB(colors.background, colors.accent, 0.42f)
            selected < 0 -> colors.background
            state.board[selected] != 0 && state.board[index] == state.board[selected] ->
                ColorUtils.blendARGB(colors.background, colors.accent, 0.23f)
            SudokuRules.arePeers(index, selected) -> ColorUtils.blendARGB(colors.background, colors.accent, 0.08f)
            else -> colors.background
        }
    }

    private fun description(state: GameState, given: Boolean, conflict: Boolean): String {
        val position = context.getString(R.string.cell_position, index / 9 + 1, index % 9 + 1)
        val value = when {
            state.board[index] != 0 -> state.board[index].toString()
            notes.isNotEmpty() -> context.getString(R.string.cell_notes, notes.sorted().joinToString(", "))
            else -> context.getString(R.string.cell_empty)
        }
        val kind = context.getString(if (given) R.string.cell_given else R.string.cell_editable)
        val warning = if (conflict) context.getString(R.string.cell_conflict) else ""
        return "$position, $value, $kind$warning"
    }
}
