package sudoku.testing

import android.app.Instrumentation
import android.graphics.Bitmap
import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

object ScreenCapture {
    fun capture(name: String) {
        val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        instrumentation.uiAutomation.waitForIdle(300, 3_000)
        save(name, requireNotNull(instrumentation.uiAutomation.takeScreenshot()))
    }

    fun save(name: String, bitmap: Bitmap) {
        val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
        val arguments: Bundle = InstrumentationRegistry.getArguments()
        val path: String = arguments.getString("additionalTestOutputDir")
            ?: instrumentation.targetContext.cacheDir.path
        val directory: File = File(path)
        directory.mkdirs()
        File(directory, name).outputStream()
            .use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
