package sudoku.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.NestedScrollView
import sudoku.R
import sudoku.home.HomeDialogs
import sudoku.ui.ThemedActivity

class SettingsActivity : ThemedActivity() {
    private lateinit var screen: SettingsScreen
    private var section = SettingsSection.MENU

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val root = findViewById<View>(R.id.settings_root)
        applySystemInsets(root)
        section = SettingsSection.entries.find { it.name == savedInstanceState?.getString("section") } ?: section
        screen = SettingsScreen(root, GamePreferences(this))
        screen.bind(::showSection) { startActivity(Intent(this, LayoutPreviewActivity::class.java)) }
        screen.render(section)
        findViewById<View>(R.id.settings_back).setOnClickListener { goBack() }
        findViewById<View>(R.id.settings_theme).setOnClickListener {
            HomeDialogs(this).showThemes(it, preferences.theme, ::selectTheme)
        }
        bindBackNavigation()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("section", section.name)
        super.onSaveInstanceState(outState)
    }

    private fun bindBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goBack()
        })
    }

    private fun showSection(selected: SettingsSection) {
        section = selected
        screen.render(section)
        findViewById<NestedScrollView>(R.id.settings_scroll).apply {
            requestFocus()
            post { scrollTo(0, 0) }
        }
    }

    private fun goBack() {
        if (section == SettingsSection.MENU) finish() else showSection(SettingsSection.MENU)
    }
}
