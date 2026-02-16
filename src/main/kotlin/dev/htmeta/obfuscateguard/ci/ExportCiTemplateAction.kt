package dev.htmeta.obfuscateguard.ci

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.dsl.builder.*
import dev.htmeta.obfuscateguard.core.ObfuscateGuardProjectService
import javax.swing.JComponent
import javax.swing.JTextArea

/**
 * Action to export CI/CD pipeline templates for obfuscation.
 */
class ExportCiTemplateAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = ObfuscateGuardProjectService.getInstance(project)
        val engineConfig = service.generateConfig()

        if (engineConfig == null) {
            Messages.showWarningDialog(project, "No configuration. Run the wizard first.", "ObfuscateGuard")
            return
        }

        CiExportDialog(project, engineConfig).show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

class CiExportDialog(
    private val project: Project,
    private val config: dev.htmeta.obfuscateguard.core.model.EngineSpecificConfig,
) : DialogWrapper(project) {

    private var selectedTemplate = "GitHub Actions"
    private val previewArea = JTextArea(20, 70).apply {
        isEditable = false
        lineWrap = true
    }

    init {
        title = "Export CI/CD Template — ObfuscateGuard"
        setOKButtonText("Save to Project")
        init()
        updatePreview()
    }

    override fun createCenterPanel(): JComponent = panel {
        group("CI/CD Platform") {
            buttonsGroup {
                row {
                    radioButton("GitHub Actions (.github/workflows/obfuscate.yml)", "GitHub Actions")
                }
                row {
                    radioButton("GitLab CI (.gitlab-ci.yml)", "GitLab CI")
                }
                row {
                    radioButton("Jenkinsfile", "Jenkinsfile")
                }
            }.bind(::selectedTemplate)
        }
        group("Preview") {
            row {
                scrollCell(previewArea).align(Align.FILL).resizableColumn()
            }.resizableRow()
        }
    }

    private fun updatePreview() {
        previewArea.text = when (selectedTemplate) {
            "GitHub Actions" -> CiTemplateGenerator.generateGitHubActions(config)
            "GitLab CI" -> CiTemplateGenerator.generateGitLabCI(config)
            "Jenkinsfile" -> CiTemplateGenerator.generateJenkinsfile(config)
            else -> ""
        }
    }

    override fun doOKAction() {
        val basePath = project.basePath ?: return
        val (filePath, content) = when (selectedTemplate) {
            "GitHub Actions" -> ".github/workflows/obfuscate.yml" to CiTemplateGenerator.generateGitHubActions(config)
            "GitLab CI" -> ".gitlab-ci.yml" to CiTemplateGenerator.generateGitLabCI(config)
            "Jenkinsfile" -> "Jenkinsfile" to CiTemplateGenerator.generateJenkinsfile(config)
            else -> return
        }

        try {
            val file = java.io.File(basePath, filePath)
            file.parentFile.mkdirs()
            file.writeText(content)

            com.intellij.notification.NotificationGroupManager.getInstance()
                .getNotificationGroup("ObfuscateGuard")
                .createNotification(
                    "CI Template Exported",
                    "Saved to $filePath",
                    com.intellij.notification.NotificationType.INFORMATION,
                )
                .notify(project)
        } catch (e: Exception) {
            Messages.showErrorDialog(project, "Failed to save: ${e.message}", "ObfuscateGuard")
        }

        super.doOKAction()
    }
}
