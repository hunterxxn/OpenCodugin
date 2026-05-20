package com.github.hunterxxn.opencodugin.terminal

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class OpenCodeTerminalSettingsTest : BasePlatformTestCase() {

    fun testMouseReportingEnabled() {
        val settings = OpenCodeTerminalSettings()
        assertTrue("Mouse reporting should be enabled", settings.enableMouseReporting())
    }

    fun testMouseActionsNotForced() {
        val settings = OpenCodeTerminalSettings()
        assertFalse("Should not force actions on mouse reporting", settings.forceActionOnMouseReporting())
    }
}
