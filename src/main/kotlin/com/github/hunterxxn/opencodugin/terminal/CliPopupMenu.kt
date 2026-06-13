package com.github.hunterxxn.opencodugin.terminal

import com.github.hunterxxn.opencodugin.MyBundle
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
        onSelected: (String?) -> Unit
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
        onSelected: (String?) -> Unit
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
        onSelected: (String?) -> Unit
    ): DefaultActionGroup {
        return DefaultActionGroup().apply {
            add(object : AnAction(MyBundle["opencode.cli.option.opencode"]) {
                override fun actionPerformed(e: AnActionEvent) {
                    val path = OpenCodeTerminalRunner.findOpencodePath(project)
                    onSelected(path)
                }
            })
            add(object : AnAction(MyBundle["opencode.cli.option.mimo"]) {
                override fun actionPerformed(e: AnActionEvent) {
                    val path = OpenCodeTerminalRunner.findCliPath("mimo")
                    onSelected(path)
                }
            })
            add(object : AnAction(MyBundle["opencode.cli.option.custom"]) {
                override fun actionPerformed(e: AnActionEvent) {
                    val input = Messages.showInputDialog(
                        project,
                        MyBundle["opencode.cli.custom.message"],
                        MyBundle["opencode.cli.custom.title"],
                        Messages.getQuestionIcon()
                    )
                    val path = input?.takeIf { it.isNotBlank() }
                    onSelected(path)
                }
            })
        }
    }
}
