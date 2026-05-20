package com.github.hunterxxn.opencodugin.commands

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

data class OpenCodeContext(
    val project: Project,
    val selectedFiles: List<VirtualFile>,
    val activeEditorFile: VirtualFile?
)

interface OpenCodeCommandProvider {
    fun buildCommand(context: OpenCodeContext): String
    fun getActionLabel(): String
}
