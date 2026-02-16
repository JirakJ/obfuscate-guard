package dev.htmeta.obfuscateguard.dryrun

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import dev.htmeta.obfuscateguard.core.ObfuscateGuardProjectService
import dev.htmeta.obfuscateguard.core.model.RiskSeverity

/**
 * Dry-run validation — simulate obfuscation and report what would break.
 * Combines risk analysis with config validation without actually running the engine.
 */
class DryRunValidationAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = ObfuscateGuardProjectService.getInstance(project)
        val config = service.config

        if (config == null) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("ObfuscateGuard")
                .createNotification(
                    "ObfuscateGuard Dry Run",
                    "No configuration found. Run the wizard first.",
                    NotificationType.WARNING,
                )
                .notify(project)
            return
        }

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "ObfuscateGuard: Dry-run validation...", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                indicator.text = "Analyzing project for obfuscation risks..."
                indicator.fraction = 0.1

                val risks = service.analyzeRisks()
                indicator.fraction = 0.4

                indicator.text = "Checking keep rules coverage..."
                val keepPatterns = config.keepRules.filter { it.accepted }.map { it.pattern }
                val uncoveredRisks = risks.filter { risk ->
                    val className = risk.className
                    keepPatterns.none { pattern -> pattern.contains(className) }
                }
                indicator.fraction = 0.6

                indicator.text = "Validating engine configuration..."
                val engineConfig = service.generateConfig()
                val configIssues = mutableListOf<String>()
                if (engineConfig == null) {
                    configIssues.add("Cannot generate engine configuration")
                } else {
                    if (engineConfig.configContent.isBlank()) {
                        configIssues.add("Generated config is empty")
                    }
                    if (!engineConfig.configContent.contains("-keep")) {
                        configIssues.add("No keep rules in generated config")
                    }
                }
                indicator.fraction = 0.8

                val criticalRisks = uncoveredRisks.count { it.severity == RiskSeverity.CRITICAL }
                val warningRisks = uncoveredRisks.count { it.severity == RiskSeverity.WARNING }
                val coveredCount = risks.size - uncoveredRisks.size

                val sb = StringBuilder()
                sb.appendLine("Dry-Run Validation Report")
                sb.appendLine("Engine: ${config.engine.displayName} | Level: ${config.level.displayName} | Keep rules: ${config.keepRules.size}")
                sb.appendLine()
                sb.appendLine("Risks: ${risks.size} total, $coveredCount covered, ${uncoveredRisks.size} uncovered ($criticalRisks critical, $warningRisks warnings)")

                if (configIssues.isNotEmpty()) {
                    sb.appendLine("Config issues: ${configIssues.joinToString("; ")}")
                }

                if (uncoveredRisks.isNotEmpty()) {
                    sb.appendLine()
                    sb.appendLine("Top uncovered risks:")
                    uncoveredRisks.take(10).forEach { risk ->
                        sb.appendLine("  ${risk.severity.icon} ${risk.className}:${risk.lineNumber} - ${risk.description}")
                    }
                }

                val verdict = when {
                    criticalRisks > 0 -> "FAIL - $criticalRisks critical risks without keep rules"
                    warningRisks > 3 -> "WARN - $warningRisks uncovered warnings"
                    configIssues.isNotEmpty() -> "WARN - Configuration issues detected"
                    else -> "PASS - All risks covered. Safe to run obfuscation."
                }
                sb.appendLine()
                sb.appendLine("Verdict: $verdict")

                indicator.fraction = 1.0

                val notificationType = when {
                    criticalRisks > 0 -> NotificationType.ERROR
                    warningRisks > 3 || configIssues.isNotEmpty() -> NotificationType.WARNING
                    else -> NotificationType.INFORMATION
                }

                NotificationGroupManager.getInstance()
                    .getNotificationGroup("ObfuscateGuard")
                    .createNotification("ObfuscateGuard Dry Run", sb.toString(), notificationType)
                    .notify(project)
            }
        })
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
