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
import dev.htmeta.obfuscateguard.policy.PolicyEngine

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

                indicator.text = "Validating engine configuration..."
                indicator.fraction = 0.5
                val configContent = service.generateConfig()?.configContent

                indicator.text = "Checking team policy..."
                indicator.fraction = 0.8
                val critical = DryRunReport.uncoveredRisks(risks, config.keepRules).count { it.severity == RiskSeverity.CRITICAL }
                val policyViolations = project.basePath?.let { PolicyEngine.loadPolicy(it) }?.let { policy ->
                    configContent?.let { PolicyEngine.validate(policy, config, it, critical) } ?: emptyList()
                }

                val result = DryRunReport.build(config, risks, configContent, policyViolations)
                indicator.fraction = 1.0
                val notificationType = when (result.outcome) {
                    DryRunOutcome.FAIL -> NotificationType.ERROR
                    DryRunOutcome.WARN -> NotificationType.WARNING
                    DryRunOutcome.PASS -> NotificationType.INFORMATION
                }
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("ObfuscateGuard")
                    .createNotification("ObfuscateGuard Dry Run", result.report, notificationType)
                    .notify(project)
            }
        })
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
