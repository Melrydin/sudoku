package sudoku.settings

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import sudoku.R
import com.google.android.material.button.MaterialButton

class SettingsScreen(private val root: View, private val preferences: GamePreferences) {
    fun bind(onSection: (SettingsSection) -> Unit, onLayout: () -> Unit) {
        root.findViewById<View>(R.id.settings_assists).setOnClickListener { onSection(SettingsSection.ASSISTS) }
        root.findViewById<View>(R.id.settings_gameplay).setOnClickListener { onSection(SettingsSection.GAMEPLAY) }
        root.findViewById<View>(R.id.settings_layout).setOnClickListener { onLayout() }
    }

    fun render(section: SettingsSection) {
        root.findViewById<TextView>(R.id.settings_title).setText(section.title)
        root.findViewById<View>(R.id.settings_menu).isVisible = section == SettingsSection.MENU
        val options = root.findViewById<LinearLayout>(R.id.settings_options)
        options.removeAllViews()
        GameSetting.entries.filter { it.section == section }.forEach { setting ->
            val row = LayoutInflater.from(root.context).inflate(R.layout.setting_toggle, options, false)
            bindToggle(row, setting)
            options.addView(row)
        }
    }

    private fun bindToggle(row: View, setting: GameSetting) {
        row.id = setting.id
        row.findViewById<TextView>(R.id.setting_title).setText(setting.title)
        row.findViewById<TextView>(R.id.setting_summary).apply {
            isVisible = setting.summary != 0
            if (setting.summary != 0) setText(setting.summary)
        }
        val toggle = row.findViewById<MaterialButton>(R.id.setting_toggle)
        toggle.isChecked = setting.read(preferences.options)
        updateLabel(toggle, setting)
        toggle.addOnCheckedChangeListener { _, checked ->
            preferences.options = setting.update(preferences.options, checked)
            updateLabel(toggle, setting)
        }
    }

    private fun updateLabel(toggle: MaterialButton, setting: GameSetting) {
        toggle.setText(if (toggle.isChecked) R.string.setting_on else R.string.setting_off)
        toggle.contentDescription = root.context.getString(setting.title)
        toggle.stateDescription = root.context.getString(
            if (toggle.isChecked) R.string.setting_on else R.string.setting_off,
        )
    }
}
