package com.github.hunterxxn.opencodugin.commands

import com.github.hunterxxn.opencodugin.terminal.OpenCodeTerminalRunner
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object ReasonixCliProvider : CliProvider {
    override val displayName = "Reasonix"
    override val defaultCommand = "reasonix"

    override fun findExecutablePath(project: Project): String =
        OpenCodeTerminalRunner.findCliPath(defaultCommand)

    override fun buildFileReference(file: VirtualFile, project: Project, editor: Editor?): String {
        val basePath = project.basePath ?: return "@${file.name}"
        val filePath = file.path
        val relativePath = if (filePath.startsWith(basePath)) {
            filePath.removePrefix(basePath).removePrefix("/").removePrefix("\\")
        } else {
            filePath
        }
        return "@$relativePath"
    }
}
