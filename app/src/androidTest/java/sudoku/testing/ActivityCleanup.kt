package sudoku.testing

import android.app.Instrumentation
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage

fun finishTestActivities() {
    val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    instrumentation.runOnMainSync {
        val monitor: ActivityLifecycleMonitor = ActivityLifecycleMonitorRegistry.getInstance()
        Stage.values().flatMap { monitor.getActivitiesInStage(it) }.distinct()
            .forEach { it.finish() }
    }
    instrumentation.waitForIdleSync()
}
