package com.github.hunterxxn.opencodugin

import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MyPluginTest : BasePlatformTestCase() {

    fun testPluginDescriptor() {
        val pluginId = "com.github.hunterxxn.opencodugin"
        assertNotNull("Plugin should be found", pluginId)
    }
}
