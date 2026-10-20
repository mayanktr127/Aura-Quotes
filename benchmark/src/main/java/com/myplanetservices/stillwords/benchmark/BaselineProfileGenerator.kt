package com.myplanetservices.stillwords.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        baselineProfileRule.collect(
            packageName = "com.myplanetservices.stillwords",
            includeInStartupProfile = true
        ) {
            // 1. App Start & Today Screen
            pressHome()
            startActivityAndWait()
            device.waitForIdle()

            // 2. Open Read feed from Today
            val readAll = device.wait(Until.findObject(By.desc("Read all")), 3000)
                ?: device.findObject(By.text("Read all"))
                ?: device.findObject(By.desc("Read"))
            if (readAll != null) {
                readAll.click()
                device.waitForIdle()
                val readPager = device.wait(Until.findObject(By.res("read_pager")), 4000)
                readPager?.let {
                    it.setGestureMargin(device.displayWidth / 5)
                    it.fling(Direction.DOWN)
                    device.waitForIdle()
                    it.fling(Direction.DOWN)
                    device.waitForIdle()
                }
                val closeButton = device.findObject(By.res("read_close_button"))
                if (closeButton != null) {
                    closeButton.click()
                } else {
                    device.pressBack()
                }
                device.waitForIdle()
            }

            // 3. Saved grid scrolling
            val savedTab = device.findObject(By.desc("Saved"))
                ?: device.findObject(By.text("Saved"))
            if (savedTab != null) {
                savedTab.click()
                device.waitForIdle()
                val savedGrid = device.wait(Until.findObject(By.res("saved_quotes_grid")), 4000)
                savedGrid?.let {
                    it.setGestureMargin(device.displayWidth / 5)
                    it.fling(Direction.DOWN)
                    device.waitForIdle()
                    it.fling(Direction.UP)
                    device.waitForIdle()
                }
            }

            // 4. Opening Quote detail and going back
            val quoteTile = device.findObject(By.res("quote_tile"))
                ?: device.findObject(By.desc("Open quote"))
            if (quoteTile != null) {
                quoteTile.click()
                device.waitForIdle()
                device.wait(Until.findObject(By.res("quote_detail_screen")), 3000)
                val backBtn = device.findObject(By.res("detail_back_button"))
                if (backBtn != null) {
                    backBtn.click()
                } else {
                    device.pressBack()
                }
                device.waitForIdle()
            }

            // 5. Opening Create and going back
            val createTab = device.findObject(By.desc("Create"))
                ?: device.findObject(By.text("Create"))
            if (createTab != null) {
                createTab.click()
                device.waitForIdle()
                val designerBack = device.findObject(By.res("designer_back_button"))
                if (designerBack != null) {
                    designerBack.click()
                } else {
                    device.pressBack()
                }
                device.waitForIdle()
            }
        }
    }
}
