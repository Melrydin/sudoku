package com.example.sudoku.ui

import android.content.Context
import java.text.NumberFormat

internal fun Context.formatNumber(value: Int): String =
    NumberFormat.getIntegerInstance(resources.configuration.locales[0]).format(value)
