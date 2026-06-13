package com.github.hunterxxn.opencodugin.commands

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface CliProvider {
    val displayName: String
    val defaultCommand: String

    fun findExecutablePath(project: Project): String
    fun buildFileReference(file: VirtualFile, project: Project, editor: Editor?): String
}
