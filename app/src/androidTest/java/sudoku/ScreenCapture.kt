package sudoku

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

object ScreenCapture {
    fun capture(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        instrumentation.uiAutomation.waitForIdle(300, 3_000)
        save(name, requireNotNull(instrumentation.uiAutomation.takeScreenshot()))
    }

    fun save(name: String, bitmap: Bitmap) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val arguments = InstrumentationRegistry.getArguments()
        val path = arguments.getString("additionalTestOutputDir") ?: instrumentation.targetContext.cacheDir.path
        val directory = File(path)
        directory.mkdirs()
        File(directory, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
