package sudoku.testing

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage

fun finishTestActivities() {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    instrumentation.runOnMainSync {
        val monitor = ActivityLifecycleMonitorRegistry.getInstance()
        Stage.values().flatMap { monitor.getActivitiesInStage(it) }.distinct().forEach { it.finish() }
    }
    instrumentation.waitForIdleSync()
}
