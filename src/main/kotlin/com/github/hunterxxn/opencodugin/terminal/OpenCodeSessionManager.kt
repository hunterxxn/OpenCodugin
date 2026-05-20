package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class OpenCodeSessionManager(private val project: Project) {

    private val panels = ConcurrentHashMap<String, OpenCodeTerminalPanel>()

    fun createPanel(workingDirectory: String): OpenCodeTerminalPanel {
        val panel = OpenCodeTerminalPanel(project, workingDirectory)
        panel.startSession()
        val session = panel.getCurrentSession()
        if (session != null) {
            panels[session.id] = panel
        }
        return panel
    }

    fun getSessions(): Map<String, OpenCodeTerminalPanel> = panels.toMap()

    fun getPanel(sessionId: String): OpenCodeTerminalPanel? = panels[sessionId]

    fun removePanel(sessionId: String) {
        panels[sessionId]?.dispose()
        panels.remove(sessionId)
    }

    fun removeAll() {
        panels.values.forEach { it.dispose() }
        panels.clear()
    }
}
