package dev.htmeta.obfuscateguard.config

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import dev.htmeta.obfuscateguard.core.ObfuscateGuardProjectService

class RunObfuscationAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = ObfuscateGuardProjectService.getInstance(project)

        if (service.config == null) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("ObfuscateGuard")
                .createNotification(
                    "ObfuscateGuard",
                    "No configuration found. Please run the configuration wizard first.",
                    NotificationType.WARNING,
                )
                .notify(project)
            return
        }

        // Find or create run configuration
        val runManager = RunManager.getInstance(project)
        val configType = ObfuscateRunConfigurationType()

        var settings = runManager.findConfigurationByTypeAndName(configType, "ObfuscateGuard")
        if (settings == null) {
            val factory = configType.configurationFactories.first()
            settings = runManager.createConfiguration("ObfuscateGuard", factory)
            runManager.addConfiguration(settings)
        }

        runManager.selectedConfiguration = settings
        ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
