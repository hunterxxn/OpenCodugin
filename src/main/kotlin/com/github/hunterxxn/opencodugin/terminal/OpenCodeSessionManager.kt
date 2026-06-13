package com.github.hunterxxn.opencodugin.terminal

import com.github.hunterxxn.opencodugin.commands.CliProvider
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class OpenCodeSessionManager(private val project: Project) {

    private val panels = ConcurrentHashMap<String, OpenCodeTerminalPanel>()

    var activeSessionId: String? = null
        private set

    fun setActiveSession(sessionId: String?) {
        activeSessionId = sessionId
    }

    fun getActivePanel(): OpenCodeTerminalPanel? {
        val id = activeSessionId ?: return null
        return panels[id]
    }

    fun createPanel(workingDirectory: String, provider: CliProvider, cliPath: String? = null): CliSession {
        val panel = OpenCodeTerminalPanel(project, workingDirectory)
        panel.startSession(provider, cliPath)
        val session = panel.getCurrentSession()
        if (session != null) {
            panels[session.id] = panel
        }
        return CliSession(panel, provider)
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

data class CliSession(val panel: OpenCodeTerminalPanel, val provider: CliProvider)
