package com.github.hunterxxn.opencodugin.terminal

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class OpenCodeTerminalSettingsTest : BasePlatformTestCase() {

    fun testMouseReportingEnabled() {
        val settings = OpenCodeTerminalSettings()
        assertTrue("Mouse reporting should be enabled", settings.enableMouseReporting())
    }

    fun testMouseActionsForced() {
        val settings = OpenCodeTerminalSettings()
        assertTrue("Should force actions on mouse reporting for text selection", settings.forceActionOnMouseReporting())
    }
}
