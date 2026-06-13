package com.github.hunterxxn.opencodugin.terminal

import com.github.hunterxxn.opencodugin.MyBundle
import com.github.hunterxxn.opencodugin.commands.CliProvider
import com.github.hunterxxn.opencodugin.commands.CliProviderRegistry
import com.github.hunterxxn.opencodugin.commands.OpenCodeCliProvider
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import javax.swing.JComponent

object CliPopupMenu {

    fun showPopup(
        project: Project,
        anchorComponent: JComponent,
        onSelected: (CliProvider, String?) -> Unit
    ) {
        val actionGroup = createCliActionGroup(project, onSelected)
        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                null,
                actionGroup,
                com.intellij.openapi.actionSystem.DataContext { dataId ->
                    if (com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT.`is`(dataId)) project else null
                },
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true
            )
        popup.showUnderneathOf(anchorComponent)
    }

    fun showPopupAtCenter(
        project: Project,
        contentComponent: JComponent,
        onSelected: (CliProvider, String?) -> Unit
    ) {
        val actionGroup = createCliActionGroup(project, onSelected)
        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                null,
                actionGroup,
                com.intellij.openapi.actionSystem.DataContext { dataId ->
                    if (com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT.`is`(dataId)) project else null
                },
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true
            )
        popup.showInCenterOf(contentComponent)
    }

    private fun createCliActionGroup(
        project: Project,
        onSelected: (CliProvider, String?) -> Unit
    ): DefaultActionGroup {
        return DefaultActionGroup().apply {
            CliProviderRegistry.getAll().forEach { provider ->
                add(object : AnAction(provider.displayName) {
                    override fun actionPerformed(e: AnActionEvent) {
                        val path = provider.findExecutablePath(project)
                        onSelected(provider, path)
                    }
                })
            }
            add(object : AnAction(MyBundle["opencode.cli.option.custom"]) {
                override fun actionPerformed(e: AnActionEvent) {
                    val input = Messages.showInputDialog(
                        project,
                        MyBundle["opencode.cli.custom.message"],
                        MyBundle["opencode.cli.custom.title"],
                        Messages.getQuestionIcon()
                    )
                    val path = input?.takeIf { it.isNotBlank() }
                    onSelected(OpenCodeCliProvider, path)
                }
            })
        }
    }
}
