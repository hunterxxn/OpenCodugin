package com.github.hunterxxn.opencodugin.commands

import com.github.hunterxxn.opencodugin.terminal.OpenCodeTerminalRunner
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object OpenCodeCliProvider : CliProvider {
    override val displayName = "OpenCode"
    override val defaultCommand = "opencode"

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

        val lineRef = if (editor != null) {
            val selectionModel = editor.selectionModel
            if (selectionModel.hasSelection()) {
                val doc = editor.document
                val startLine = doc.getLineNumber(selectionModel.selectionStart) + 1
                val endLine = doc.getLineNumber(selectionModel.selectionEnd) + 1
                if (startLine == endLine) "#L$startLine" else "#L$startLine-$endLine"
            } else {
                null
            }
        } else {
            null
        }

        return " @$relativePath${lineRef ?: ""} "
    }
}
