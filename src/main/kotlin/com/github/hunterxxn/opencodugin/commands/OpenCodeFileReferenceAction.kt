package com.github.hunterxxn.opencodugin.commands

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile

class OpenCodeFileReferenceAction : AnAction(), DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        val files = getSelectedFiles(e)
        e.presentation.isEnabledAndVisible = project != null && files.isNotEmpty()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val files = getSelectedFiles(e)
        if (files.isEmpty()) return

        val editor = e.getData(CommonDataKeys.EDITOR)

        val refs = files.map { file ->
            OpenCodeContextInjector.formatFileRef(file, project, editor)
        }

        val text = refs.joinToString(" ")
        OpenCodeContextInjector.injectToTerminal(project, text)
    }

    private fun getSelectedFiles(e: AnActionEvent): List<VirtualFile> {
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        if (!files.isNullOrEmpty()) return files.toList()

        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file != null) return listOf(file)

        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        psiFile?.virtualFile?.let { return listOf(it) }

        val navArray = e.getData(CommonDataKeys.NAVIGATABLE_ARRAY)
        if (navArray != null) {
            return navArray.mapNotNull { nav ->
                (nav as? com.intellij.psi.PsiElement)?.containingFile?.virtualFile
            }.distinct()
        }

        val selectedItems = e.getData(PlatformCoreDataKeys.SELECTED_ITEMS)
        if (selectedItems != null) {
            return selectedItems.mapNotNull { item ->
                when (item) {
                    is VirtualFile -> item
                    is com.intellij.psi.PsiFile -> item.virtualFile
                    is com.intellij.psi.PsiDirectory -> item.virtualFile
                    else -> null
                }
            }.distinct()
        }

        return emptyList()
    }
}
