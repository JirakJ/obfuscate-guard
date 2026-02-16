package dev.htmeta.obfuscateguard.analyzer

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import dev.htmeta.obfuscateguard.core.ObfuscateGuardProjectService

class AnalyzeRisksAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Analyzing obfuscation risks...", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                val service = ObfuscateGuardProjectService.getInstance(project)
                val results = service.analyzeRisks()

                val critical = results.count { it.severity == dev.htmeta.obfuscateguard.core.model.RiskSeverity.CRITICAL }
                val warnings = results.count { it.severity == dev.htmeta.obfuscateguard.core.model.RiskSeverity.WARNING }

                NotificationGroupManager.getInstance()
                    .getNotificationGroup("ObfuscateGuard")
                    .createNotification(
                        "ObfuscateGuard Analysis Complete",
                        "Found ${results.size} risks ($critical critical, $warnings warnings)",
                        if (critical > 0) NotificationType.WARNING else NotificationType.INFORMATION,
                    )
                    .notify(project)
            }
        })
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
