package sudoku.testing

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferenceSnapshot(private val context: Context, private vararg val names: String) {
    private var saved: Map<String, Map<String, *>> = emptyMap()

    fun captureAndClear() {
        saved = names.associateWith { preferences(it).all.toMap() }
        names.forEach { preferences(it).edit(commit = true) { clear() } }
    }

    fun restore() {
        saved.forEach { (name, values) ->
            preferences(name).edit(commit = true) {
                clear()
                values.forEach { (key, value) -> restoreValue(this, key, value) }
            }
        }
        saved = emptyMap()
    }

    private fun preferences(name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    private fun restoreValue(editor: SharedPreferences.Editor, key: String, value: Any?) {
        when (value) {
            is String -> editor.putString(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
            null -> editor.remove(key)
        }
    }
}
