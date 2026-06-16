package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.TabsListener
import com.intellij.ui.tabs.JBTabs
import com.intellij.ui.tabs.JBTabsFactory
import com.github.hunterxxn.opencodugin.MyBundle
import com.github.hunterxxn.opencodugin.update.CheckUpdateAction
import com.intellij.ui.Gray
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.Box
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JToolBar
import javax.swing.SwingUtilities
import java.awt.BorderLayout
import kotlin.concurrent.thread

class OpenCodeToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().info("OpenCodeToolWindowFactory initialized")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val sessionManager = project.service<OpenCodeSessionManager>()
        val tabs = JBTabsFactory.createTabs(project)

        val mainPanel = JPanel(BorderLayout())
        val placeholder = JPanel()
        val toolbarResult = createToolbar(project, sessionManager, tabs, mainPanel, placeholder)
        mainPanel.add(toolbarResult.first, BorderLayout.NORTH)
        val newSessionButton = toolbarResult.second

        mainPanel.add(placeholder, BorderLayout.CENTER)

        val content = ContentFactory.getInstance().createContent(mainPanel, null, false)
        toolWindow.contentManager.addContent(content)

        SwingUtilities.invokeLater {
            newSessionButton.doClick()
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
        tabs: JBTabs,
        mainPanel: JPanel,
        placeholder: JPanel
    ): Pair<JToolBar, JButton> {
        val toolbar = JToolBar()
        toolbar.isFloatable = false

        val newSessionButton = JButton(MyBundle["opencode.session.new"])
        newSessionButton.addActionListener {
            val workingDir = project.basePath ?: System.getProperty("user.home")
            CliPopupMenu.showPopup(project, newSessionButton) { provider, cliPath ->
                if (cliPath != null) {
                    mainPanel.remove(placeholder)
                    mainPanel.add(tabs.component, BorderLayout.CENTER)
                    mainPanel.revalidate()
                    mainPanel.repaint()

                    thread(name = "OpenCode-Session-Starter", isDaemon = true) {
                        val cliSession = sessionManager.createPanel(workingDir, provider, cliPath)
                        val session = cliSession.panel.getCurrentSession()
                        if (session != null) {
                            SwingUtilities.invokeLater {
                                val tabInfo = TabInfo(cliSession.panel.component).apply {
                                    setText("${cliSession.provider.displayName} ${tabs.tabCount + 1}")
                                }
                                val closeIcon = object : Icon {
                                    override fun getIconWidth() = 14
                                    override fun getIconHeight() = 14
                                    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
                                        val g2 = g.create() as Graphics2D
                                        try {
                                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                                            val isHovered = try {
                                                c != null && c.isShowing && c.mousePosition != null
                                            } catch (_: Exception) { false }
                                            if (isHovered) {
                                                g2.color = JBColor(Color(180, 180, 180, 40), Color(180, 180, 180, 30))
                                                g2.fillOval(x, y, 14, 14)
                                                g2.color = JBColor(Gray._60, Gray._40)
                                            } else {
                                                g2.color = JBColor(Gray._120, Gray._160)
                                            }
                                            g2.stroke = BasicStroke(1.8f)
                                            g2.drawLine(x + 3, y + 3, x + 11, y + 11)
                                            g2.drawLine(x + 11, y + 3, x + 3, y + 11)
                                        } finally {
                                            g2.dispose()
                                        }
                                    }
                                }
                                tabInfo.setTabLabelActions(DefaultActionGroup(object : AnAction(closeIcon) {
                                    override fun actionPerformed(e: AnActionEvent) {
                                        sessionManager.removePanel(session.id)
                                        tabs.removeTab(tabInfo)
                                    }
                                    override fun update(e: AnActionEvent) {
                                        e.presentation.isEnabled = true
                                        e.presentation.isVisible = true
                                    }
                                }), "opencode-tab-close")
                                tabs.addTab(tabInfo)
                                tabs.select(tabInfo, true)
                                sessionManager.setActiveSession(session.id)
                            }
                        }
                    }
                }
            }
        }
        toolbar.add(newSessionButton)

        toolbar.add(Box.createHorizontalGlue())

        toolbar.add(JButton(MyBundle["opencode.switchFont"]).apply {
            addActionListener {
                val popup = JPopupMenu()
                popup.add(JMenuItem(MyBundle["opencode.switchFont.embedded"]).apply {
                    addActionListener {
                        OpenCodeTerminalSettings.setFontPreference(true)
                        val font = OpenCodeTerminalSettings.computeFont()
                        sessionManager.getSessions().values.forEach { it.applyFont(font) }
                    }
                })
                popup.add(JMenuItem(MyBundle["opencode.switchFont.default"]).apply {
                    addActionListener {
                        OpenCodeTerminalSettings.setFontPreference(false)
                        val font = OpenCodeTerminalSettings.computeFont()
                        sessionManager.getSessions().values.forEach { it.applyFont(font) }
                    }
                })
                popup.show(this, 0, this.height)
            }
        })

        toolbar.add(JButton(MyBundle["opencode.checkUpdate"]).apply {
            addActionListener {
                CheckUpdateAction.performCheck(project)
            }
        })

        return Pair(toolbar, newSessionButton)
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
