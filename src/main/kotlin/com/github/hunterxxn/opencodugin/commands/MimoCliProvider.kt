package com.github.hunterxxn.opencodugin.commands

import com.github.hunterxxn.opencodugin.terminal.OpenCodeTerminalRunner
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object MimoCliProvider : CliProvider {
    override val displayName = "Mimo"
    override val defaultCommand = "mimo"

    override fun findExecutablePath(project: Project): String =
        OpenCodeTerminalRunner.findCliPath(defaultCommand)

    override fun buildFileReference(file: VirtualFile, project: Project, editor: Editor?): String {
        return OpenCodeCliProvider.buildFileReference(file, project, editor)
    }
}
