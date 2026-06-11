package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.TabsListener
import com.intellij.ui.tabs.impl.JBTabsImpl
import com.github.hunterxxn.opencodugin.MyBundle
import com.github.hunterxxn.opencodugin.update.CheckUpdateAction
import javax.swing.Box
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
        val cliPath = CliSelectorDialog.selectCli(project)
        if (cliPath != null) {
            val cliSession = sessionManager.createPanel(defaultWorkingDir, cliPath)
            val defaultSession = cliSession.panel.getCurrentSession()
            if (defaultSession != null) {
                val tabInfo = TabInfo(cliSession.panel.component).apply {
                    setText("${cliSession.cliName} 1")
                }
                tabs.addTab(tabInfo)
                tabs.select(tabInfo, true)
                sessionManager.setActiveSession(defaultSession.id)
            }
        }

        tabs.addListener(object : TabsListener {
            override fun selectionChanged(oldSelection: TabInfo?, newSelection: TabInfo?) {
                if (newSelection != null) {
                    val panel = findPanelForTab(sessionManager, newSelection)
                    val session = panel?.getCurrentSession()
                    sessionManager.setActiveSession(session?.id)
                } else {
                    sessionManager.setActiveSession(null)
                }
            }
        })
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
                val cliPath = CliSelectorDialog.selectCli(project) ?: return@addActionListener
                val cliSession = sessionManager.createPanel(workingDir, cliPath)
                val session = cliSession.panel.getCurrentSession()
                if (session != null) {
                    val tabInfo = TabInfo(cliSession.panel.component).apply {
                        setText("${cliSession.cliName} ${tabs.tabCount + 1}")
                    }
                    tabs.addTab(tabInfo)
                    tabs.select(tabInfo, true)
                    sessionManager.setActiveSession(session.id)
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

        toolbar.add(Box.createHorizontalGlue())

        toolbar.add(JButton(MyBundle["opencode.checkUpdate"]).apply {
            addActionListener {
                CheckUpdateAction.performCheck(project)
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
