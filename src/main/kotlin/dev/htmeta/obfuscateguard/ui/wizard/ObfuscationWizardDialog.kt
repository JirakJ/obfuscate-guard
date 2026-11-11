package dev.htmeta.obfuscateguard.ui.wizard

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import dev.htmeta.obfuscateguard.core.ObfuscateGuardProjectService
import dev.htmeta.obfuscateguard.core.model.*
import dev.htmeta.obfuscateguard.core.spi.ObfuscationEngineAdapter
import java.awt.CardLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * 5-step configuration wizard for setting up obfuscation.
 */
class ObfuscationWizardDialog(private val project: Project) : DialogWrapper(project) {

    private val service = ObfuscateGuardProjectService.getInstance(project)
    private val profile = service.detectProject()

    private var selectedEngine = EngineType.PROGUARD
    private var selectedLevel = ObfuscationLevel.entries
        .firstOrNull { it.name == dev.htmeta.obfuscateguard.ui.settings.ObfuscateGuardSettings.getInstance().defaultLevel }
        ?: ObfuscationLevel.STANDARD
    private val selectedGoals = mutableSetOf(ProtectionGoal.IP_PROTECTION)

    private var currentStep = 0
    private val cardLayout = CardLayout()
    private val cardPanel = JPanel(cardLayout)
    private val stepNames = listOf("project", "engine", "level", "goals", "review")

    init {
        title = "Configure Obfuscation — ObfuscateGuard"
        setOKButtonText("Apply Configuration")
        init()
    }

    override fun createCenterPanel(): JComponent {
        cardPanel.add(createProjectStep(), "project")
        cardPanel.add(createEngineStep(), "engine")
        cardPanel.add(createLevelStep(), "level")
        cardPanel.add(createGoalsStep(), "goals")
        cardPanel.add(createReviewStep(), "review")
        return cardPanel
    }

    override fun createSouthAdditionalPanel(): JPanel {
        return panel {
            row {
                button("< Back") {
                    if (currentStep > 0) {
                        currentStep--
                        cardLayout.show(cardPanel, stepNames[currentStep])
                        updateButtons()
                    }
                }
                button("Next >") {
                    if (currentStep < stepNames.size - 1) {
                        currentStep++
                        if (currentStep == stepNames.size - 1) refreshReviewStep()
                        cardLayout.show(cardPanel, stepNames[currentStep])
                        updateButtons()
                    }
                }
            }
        }
    }

    private fun updateButtons() {
        isOKActionEnabled = currentStep == stepNames.size - 1
    }

    override fun doOKAction() {
        val config = ObfuscationConfig(
            engine = selectedEngine,
            level = selectedLevel,
            goals = selectedGoals.toSet(),
        )
        service.updateConfig(config)

        // Generate engine-specific config
        val adapter = ObfuscationEngineAdapter.findAdapter(selectedEngine)
        if (adapter != null) {
            val engineConfig = adapter.generateConfig(profile, config)
            writeConfigToProject(engineConfig)
        }

        super.doOKAction()
    }

    private fun createProjectStep(): JComponent = panel {
        group("Step 1: Project Type") {
            row("Project:") {
                label(profile.projectName.ifBlank { "Unknown" })
            }
            row("Build system:") {
                label(profile.buildSystem.displayName)
            }
            row("Project type:") {
                label(profile.projectType.displayName)
            }
            row("Modules:") {
                label(if (profile.modules.isEmpty()) "Single module" else "${profile.modules.size} modules")
            }
            if (profile.detectedEngines.isNotEmpty()) {
                row("Detected engines:") {
                    label(profile.detectedEngines.joinToString { it.displayName })
                }
            }
            row {
                comment("Auto-detected project configuration. Proceed to select your obfuscation engine.")
            }
        }
    }

    private fun createEngineStep(): JComponent = panel {
        group("Step 2: Engine Selection") {
            buttonsGroup {
                // Only engines with a registered adapter can generate a configuration and run.
                for (engine in EngineType.entries.filter { ObfuscationEngineAdapter.findAdapter(it) != null }) {
                    row {
                        radioButton(engine.displayName, engine)
                            .comment(
                                when (engine) {
                                    EngineType.PROGUARD -> "Most popular for Android and Java. Free, well-documented."
                                    EngineType.YGUARD -> "Ant-based bytecode obfuscator for Java applications."
                                    EngineType.ALLATORI -> "Commercial full-featured obfuscator with string encryption."
                                }
                            )
                    }
                }
            }.bind(::selectedEngine)
        }
    }

    private fun createLevelStep(): JComponent = panel {
        group("Step 3: Obfuscation Level") {
            buttonsGroup {
                for (level in ObfuscationLevel.entries) {
                    row {
                        radioButton(level.displayName, level)
                            .comment(level.description)
                    }
                }
            }.bind(::selectedLevel)
        }
    }

    private fun createGoalsStep(): JComponent = panel {
        group("Step 4: Protection Goals") {
            for (goal in ProtectionGoal.entries) {
                row {
                    checkBox(goal.displayName)
                        .applyToComponent {
                            isSelected = goal in selectedGoals
                            addActionListener {
                                if (isSelected) selectedGoals.add(goal) else selectedGoals.remove(goal)
                            }
                        }
                }
            }
            row {
                comment("Select what you want to protect. More goals = more aggressive obfuscation rules.")
            }
        }
    }

    private var reviewPanel: JPanel? = null

    private fun createReviewStep(): JComponent {
        reviewPanel = JPanel().apply { layout = java.awt.BorderLayout() }
        refreshReviewStep()
        return reviewPanel!!
    }

    private fun refreshReviewStep() {
        val adapter = ObfuscationEngineAdapter.findAdapter(selectedEngine) ?: return
        val config = ObfuscationConfig(engine = selectedEngine, level = selectedLevel, goals = selectedGoals.toSet())
        val engineConfig = adapter.generateConfig(profile, config)

        reviewPanel?.removeAll()
        reviewPanel?.add(
            panel {
                group("Step 5: Review Configuration") {
                    row("Engine:") { label(selectedEngine.displayName) }
                    row("Level:") { label(selectedLevel.displayName) }
                    row("Goals:") { label(selectedGoals.joinToString { it.displayName }) }
                    row {
                        comment("Generated ProGuard/R8 configuration:")
                    }
                    row {
                        textArea()
                            .applyToComponent {
                                text = engineConfig.configContent
                                rows = 15
                                columns = 60
                                isEditable = false
                            }
                            .align(Align.FILL)
                    }.resizableRow()
                }
            },
            java.awt.BorderLayout.CENTER,
        )
        reviewPanel?.revalidate()
        reviewPanel?.repaint()
    }

    private fun writeConfigToProject(config: EngineSpecificConfig) {
        val basePath = profile.basePath
        if (basePath.isBlank()) return
        val configFile = java.io.File(basePath, config.configFilePath ?: "proguard-rules.pro")
        try {
            // Never destroy a hand-written config silently
            if (configFile.exists()) {
                configFile.copyTo(java.io.File(configFile.path + ".bak"), overwrite = true)
            }
            configFile.writeText(config.configContent)
            com.intellij.openapi.vfs.LocalFileSystem.getInstance().refreshIoFiles(listOf(configFile))
        } catch (e: Exception) {
            com.intellij.openapi.diagnostic.Logger.getInstance(javaClass).warn("Failed to write config", e)
        }
    }
}
