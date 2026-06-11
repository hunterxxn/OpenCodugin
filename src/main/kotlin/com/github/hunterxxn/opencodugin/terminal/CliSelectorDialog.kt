package com.github.hunterxxn.opencodugin.terminal

import com.github.hunterxxn.opencodugin.MyBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

object CliSelectorDialog {

    private const val OPTION_OPENCODE = 0
    private const val OPTION_MIMO = 1
    private const val OPTION_CUSTOM = 2

    fun selectCli(project: Project): String? {
        val options = arrayOf(
            MyBundle["opencode.cli.option.opencode"],
            MyBundle["opencode.cli.option.mimo"],
            MyBundle["opencode.cli.option.custom"]
        )

        val choice = Messages.showDialog(
            project,
            MyBundle["opencode.cli.select.message"],
            MyBundle["opencode.cli.select.title"],
            options,
            OPTION_OPENCODE,
            Messages.getQuestionIcon()
        )

        return when (choice) {
            OPTION_OPENCODE -> OpenCodeTerminalRunner.findOpencodePath(project)
            OPTION_MIMO -> OpenCodeTerminalRunner.findCliPath("mimo")
            OPTION_CUSTOM -> showCustomInput(project)
            else -> null
        }
    }

    private fun showCustomInput(project: Project): String? {
        val input = Messages.showInputDialog(
            project,
            MyBundle["opencode.cli.custom.message"],
            MyBundle["opencode.cli.custom.title"],
            Messages.getQuestionIcon()
        )
        return input?.takeIf { it.isNotBlank() }
    }
}
