package com.github.hunterxxn.opencodugin.commands

import com.github.hunterxxn.opencodugin.MyBundle
import com.github.hunterxxn.opencodugin.terminal.OpenCodeSessionManager
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.datatransfer.StringSelection
import java.nio.charset.StandardCharsets

object OpenCodeContextInjector {

    fun formatFileRef(file: VirtualFile, project: Project, editor: Editor?): String {
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

    fun injectToTerminal(project: Project, text: String): Boolean {
        val sessionManager = project.service<OpenCodeSessionManager>()
        val panel = sessionManager.getActivePanel()
        val session = panel?.getCurrentSession()

        return if (session != null && session.isAlive) {
            try {
                val bytes = text.toByteArray(StandardCharsets.UTF_8)
                session.process.outputStream.write(bytes)
                session.process.outputStream.flush()
                thisLogger().info("Injected text to OpenCode session: $text")
                true
            } catch (e: Exception) {
                thisLogger().error("Failed to inject text to terminal", e)
                fallbackToClipboard(text)
                false
            }
        } else {
            fallbackToClipboard(text)
            false
        }
    }

    private fun fallbackToClipboard(text: String) {
        CopyPasteManager.getInstance().setContents(StringSelection(text))
        showNotification(MyBundle["opencode.action.addFileRef.copied"])
    }

    private fun showNotification(message: String) {
        try {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("OpenCode Notifications")
                .createNotification(message, NotificationType.INFORMATION)
                .notify(null)
        } catch (_: Exception) {
            thisLogger().warn("Failed to show notification: $message")
        }
    }
}
