package com.myplanetservices.stillwords.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollReadFeedNoCompilation() = scrollReadFeed(CompilationMode.None())

    @Test
    fun scrollReadFeedBaselineProfile() = scrollReadFeed(CompilationMode.Partial())

    @Test
    fun scrollSavedGridNoCompilation() = scrollSavedGrid(CompilationMode.None())

    @Test
    fun scrollSavedGridBaselineProfile() = scrollSavedGrid(CompilationMode.Partial())

    private fun scrollReadFeed(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = "com.myplanetservices.stillwords",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = compilationMode,
        iterations = 5,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.waitForIdle()

            // Dismiss onboarding if present
            val skipBtn = device.findObject(By.text("Skip for now"))
                ?: device.findObject(By.text("Skip"))
            skipBtn?.click()
            device.waitForIdle()

            // If not already on read pager, navigate from hero card or Read tab
            var pager = device.findObject(By.res("read_pager"))
            if (pager == null) {
                val heroCard = device.wait(Until.findObject(By.res("today_hero_card")), 3000)
                    ?: device.findObject(By.text("Read all"))
                heroCard?.click()
                device.wait(Until.findObject(By.res("read_pager")), 4000)
            }
            device.waitForIdle()
        }
    ) {
        val pager = device.wait(Until.findObject(By.res("read_pager")), 2000)
            ?: device.findObject(By.res("read_pager"))
        pager?.setGestureMargin(device.displayWidth / 5)
        pager?.fling(Direction.DOWN)
        device.waitForIdle()
        pager?.fling(Direction.UP)
        device.waitForIdle()
    }

    private fun scrollSavedGrid(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = "com.myplanetservices.stillwords",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = compilationMode,
        iterations = 5,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.waitForIdle()

            // Dismiss onboarding if present
            val skipBtn = device.findObject(By.text("Skip for now"))
                ?: device.findObject(By.text("Skip"))
            skipBtn?.click()
            device.waitForIdle()

            // If on ReadScreen or DetailScreen, press back to return to main tabs
            if (device.findObject(By.res("read_pager")) != null) {
                device.pressBack()
                device.waitForIdle()
            }

            // Navigate to Saved tab if not already on grid
            var grid = device.findObject(By.res("saved_quotes_grid"))
            if (grid == null) {
                val savedTab = device.wait(Until.findObject(By.desc("Saved")), 3000)
                    ?: device.findObject(By.text("Saved"))
                savedTab?.click()
                device.wait(Until.findObject(By.res("saved_quotes_grid")), 4000)
            }
            device.waitForIdle()
        }
    ) {
        val grid = device.wait(Until.findObject(By.res("saved_quotes_grid")), 2000)
            ?: device.findObject(By.res("saved_quotes_grid"))
        grid?.setGestureMargin(device.displayWidth / 5)
        grid?.fling(Direction.DOWN)
        device.waitForIdle()
        grid?.fling(Direction.UP)
        device.waitForIdle()
    }
}
