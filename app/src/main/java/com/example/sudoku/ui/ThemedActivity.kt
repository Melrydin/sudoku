package com.example.sudoku.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sudoku.home.HomePreferences
import com.example.sudoku.home.HomeTheme

abstract class ThemedActivity : AppCompatActivity() {
    protected val preferences: HomePreferences by lazy { HomePreferences(this) }
    private lateinit var appliedTheme: HomeTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        appliedTheme = preferences.theme
        setTheme(appliedTheme.style)
        super.onCreate(savedInstanceState)
        configureSystemBars()
    }

    protected fun applySystemInsets(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    protected fun selectTheme(theme: HomeTheme) {
        if (theme == preferences.theme) return
        preferences.theme = theme
        recreate()
    }

    protected fun refreshTheme(): Boolean {
        if (appliedTheme == preferences.theme) return false
        recreate()
        return true
    }

    private fun configureSystemBars() {
        val style = if (preferences.theme.lightSystemBars) {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        } else {
            SystemBarStyle.dark(Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}
