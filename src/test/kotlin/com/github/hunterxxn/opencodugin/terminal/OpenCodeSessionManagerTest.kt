package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class OpenCodeSessionManagerTest : BasePlatformTestCase() {

    fun testServiceRegistered() {
        val manager = project.service<OpenCodeSessionManager>()
        assertNotNull("OpenCodeSessionManager should be registered as project service", manager)
    }

    fun testInitialStateEmpty() {
        val manager = project.service<OpenCodeSessionManager>()
        assertTrue("Sessions should be empty initially", manager.getSessions().isEmpty())
    }
}
