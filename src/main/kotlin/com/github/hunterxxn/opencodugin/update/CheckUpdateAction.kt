package com.github.hunterxxn.opencodugin.update

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.github.hunterxxn.opencodugin.MyBundle

class CheckUpdateAction : AnAction(
    MyBundle["opencode.checkUpdate"],
    MyBundle["opencode.checkUpdate"],
    null
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        performCheck(project)
    }

    companion object {
        fun performCheck(project: Project) {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, MyBundle["opencode.update.checking"], false) {
                private var result: UpdateResult? = null

                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = true
                    result = UpdateChecker.checkForUpdate()
                }

                override fun onSuccess() {
                    handleResult(project, result)
                }
            })
        }

        private fun handleResult(project: Project, result: UpdateResult?) {
            when (result) {
                is UpdateResult.Available -> showUpdateAvailableDialog(project, result)
                is UpdateResult.UpToDate -> showUpToDateNotification(project)
                is UpdateResult.Error -> showErrorNotification(project, result.message)
                null -> showErrorNotification(project, "Unknown error")
            }
        }

        private fun showUpdateAvailableDialog(project: Project, result: UpdateResult.Available) {
            val currentVersion = UpdateChecker.getCurrentVersion() ?: "?"
            val message = MyBundle["opencode.update.available.message", currentVersion, result.latestVersion]

            val choice = Messages.showYesNoDialog(
                project,
                message,
                MyBundle["opencode.update.available.title"],
                MyBundle["opencode.update.gotoRelease"],
                MyBundle["opencode.update.cancel"],
                Messages.getQuestionIcon()
            )

            if (choice == Messages.YES) {
                BrowserUtil.browse(result.downloadUrl)
            }
        }

        private fun showUpToDateNotification(project: Project) {
            val currentVersion = UpdateChecker.getCurrentVersion() ?: "?"
            val notifGroup = NotificationGroupManager.getInstance().getNotificationGroup("OpenCode Notifications")
            notifGroup.createNotification(
                MyBundle["opencode.update.uptodate.title"],
                MyBundle["opencode.update.uptodate.message", currentVersion],
                NotificationType.INFORMATION
            ).notify(project)
        }

        private fun showErrorNotification(project: Project, message: String) {
            val notifGroup = NotificationGroupManager.getInstance().getNotificationGroup("OpenCode Notifications")
            notifGroup.createNotification(
                MyBundle["opencode.update.error.title"],
                MyBundle["opencode.update.error.message", message],
                NotificationType.ERROR
            ).notify(project)
        }
    }
}
