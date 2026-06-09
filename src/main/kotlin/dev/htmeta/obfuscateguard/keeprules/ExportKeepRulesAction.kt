package dev.htmeta.obfuscateguard.keeprules

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import dev.htmeta.obfuscateguard.core.ObfuscateGuardProjectService
import java.io.File

/**
 * Writes the keep rules collected from risk analysis and quick-fixes into
 * the project's proguard-rules.pro, preserving hand-written content.
 */
class ExportKeepRulesAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = ObfuscateGuardProjectService.getInstance(project)

        val rules = (service.config?.keepRules ?: emptyList())
            .ifEmpty { service.analysisResults.mapNotNull { it.suggestedKeepRule } }
            .distinctBy { it.pattern }

        if (rules.isEmpty()) {
            Messages.showInfoMessage(
                project,
                "No keep rules available. Run 'Analyze Obfuscation Risks' first.",
                "ObfuscateGuard",
            )
            return
        }

        val basePath = project.basePath ?: return
        val configFile = File(basePath, "proguard-rules.pro")

        try {
            val existing = if (configFile.isFile) configFile.readText() else ""
            configFile.writeText(KeepRulesExporter.merge(existing, rules))
            LocalFileSystem.getInstance().refreshIoFiles(listOf(configFile))

            NotificationGroupManager.getInstance()
                .getNotificationGroup("ObfuscateGuard")
                .createNotification(
                    "Keep rules exported",
                    "${rules.size} keep rule(s) written to proguard-rules.pro",
                    NotificationType.INFORMATION,
                )
                .notify(project)
        } catch (ex: Exception) {
            Messages.showErrorDialog(project, "Failed to write proguard-rules.pro: ${ex.message}", "ObfuscateGuard")
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
