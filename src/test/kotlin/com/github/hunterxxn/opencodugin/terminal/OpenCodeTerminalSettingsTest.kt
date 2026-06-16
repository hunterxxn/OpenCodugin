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

    fun testDefaultFontPreferenceIsEmbedded() {
        assertTrue("Default font preference should be embedded", OpenCodeTerminalSettings.isEmbeddedFontEnabled())
    }

    fun testSetFontPreferenceToDefault() {
        OpenCodeTerminalSettings.setFontPreference(false)
        assertFalse("Font preference should be default", OpenCodeTerminalSettings.isEmbeddedFontEnabled())
        OpenCodeTerminalSettings.setFontPreference(true)
    }

    fun testSetFontPreferenceToEmbedded() {
        OpenCodeTerminalSettings.setFontPreference(false)
        OpenCodeTerminalSettings.setFontPreference(true)
        assertTrue("Font preference should be embedded", OpenCodeTerminalSettings.isEmbeddedFontEnabled())
    }

    fun testComputeFontReturnsNonNull() {
        val font = OpenCodeTerminalSettings.computeFont()
        assertNotNull("computeFont should return a non-null font", font)
    }
}
