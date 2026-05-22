package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.impl.JBTabsImpl
import com.github.hunterxxn.opencodugin.MyBundle
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JToolBar
import java.awt.BorderLayout

class OpenCodeToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().info("OpenCodeToolWindowFactory initialized")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val sessionManager = project.service<OpenCodeSessionManager>()
        val tabs = JBTabsImpl(project)

        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(createToolbar(project, sessionManager, tabs), BorderLayout.NORTH)
        mainPanel.add(tabs.component, BorderLayout.CENTER)

        val content = ContentFactory.getInstance().createContent(mainPanel, null, false)
        toolWindow.contentManager.addContent(content)

        val defaultWorkingDir = project.basePath ?: System.getProperty("user.home")
        val defaultPanel = sessionManager.createPanel(defaultWorkingDir)
        val defaultSession = defaultPanel.getCurrentSession()
        if (defaultSession != null) {
            val tabInfo = TabInfo(defaultPanel.component).apply {
                setText(MyBundle["opencode.session.tab.default"])
            }
            tabs.addTab(tabInfo)
            tabs.select(tabInfo, true)
        }
    }

    override fun shouldBeAvailable(project: Project): Boolean = true

    private fun createToolbar(
        project: Project,
        sessionManager: OpenCodeSessionManager,
        tabs: JBTabsImpl
    ): JToolBar {
        val toolbar = JToolBar()
        toolbar.isFloatable = false

        toolbar.add(JButton(MyBundle["opencode.session.new"]).apply {
            addActionListener {
                val workingDir = project.basePath ?: System.getProperty("user.home")
                val panel = sessionManager.createPanel(workingDir)
                val session = panel.getCurrentSession()
                if (session != null) {
                    val tabInfo = TabInfo(panel.component).apply {
                        setText("${MyBundle["opencode.session.tab.default"]} ${tabs.tabCount + 1}")
                    }
                    tabs.addTab(tabInfo)
                    tabs.select(tabInfo, true)
                }
            }
        })

        toolbar.add(JButton(MyBundle["opencode.session.stop"]).apply {
            addActionListener {
                val selectedInfo = tabs.selectedInfo ?: return@addActionListener
                val panel = findPanelForTab(sessionManager, selectedInfo) ?: return@addActionListener
                val session = panel.getCurrentSession()
                if (session != null) {
                    sessionManager.removePanel(session.id)
                }
                tabs.removeTab(selectedInfo)
            }
        })

        return toolbar
    }

    private fun findPanelForTab(
        sessionManager: OpenCodeSessionManager,
        tabInfo: TabInfo
    ): OpenCodeTerminalPanel? {
        for ((_, panel) in sessionManager.getSessions()) {
            if (panel.component == tabInfo.component) {
                return panel
            }
        }
        return null
    }
}
