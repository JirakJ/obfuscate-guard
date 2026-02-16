package dev.htmeta.obfuscateguard.config

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.panel
import dev.htmeta.obfuscateguard.core.ObfuscateGuardProjectService
import dev.htmeta.obfuscateguard.core.spi.ObfuscationEngineAdapter
import javax.swing.Icon
import javax.swing.JComponent

class ObfuscateRunConfigurationType : ConfigurationType {
    override fun getDisplayName() = "ObfuscateGuard"
    override fun getConfigurationTypeDescription() = "Run obfuscation with ObfuscateGuard"
    override fun getIcon(): Icon = com.intellij.icons.AllIcons.Actions.Execute
    override fun getId() = "ObfuscateGuardRunConfiguration"
    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(ObfuscateRunConfigurationFactory(this))
}

class ObfuscateRunConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId() = "ObfuscateGuardRunConfigurationFactory"
    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        ObfuscateRunConfiguration(project, this, "ObfuscateGuard")
}

class ObfuscateRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String,
) : RunConfigurationBase<RunConfigurationOptions>(project, factory, name) {

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        ObfuscateRunSettingsEditor()

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return RunProfileState { _, _ ->
            val service = ObfuscateGuardProjectService.getInstance(project)
            val config = service.config
            val engineConfig = service.generateConfig()

            if (config == null || engineConfig == null) {
                throw RuntimeException("No obfuscation configuration. Run the wizard first.")
            }

            val adapter = ObfuscationEngineAdapter.findAdapter(config.engine)
                ?: throw RuntimeException("Engine adapter not found: ${config.engine}")

            val command = adapter.buildRunCommand(project, engineConfig)
            val commandLine = GeneralCommandLine(command)
                .withWorkDirectory(project.basePath)

            val handler = ProcessHandlerFactory.getInstance().createProcessHandler(commandLine)
            com.intellij.execution.DefaultExecutionResult(
                com.intellij.execution.impl.ConsoleViewImpl(project, false),
                handler,
            )
        }
    }
}

class ObfuscateRunSettingsEditor : SettingsEditor<ObfuscateRunConfiguration>() {
    override fun createEditor(): JComponent = panel {
        row {
            comment("ObfuscateGuard will run obfuscation using the current project configuration.")
        }
        row {
            comment("Use Tools → ObfuscateGuard → Configure Obfuscation to set up the configuration first.")
        }
    }

    override fun applyEditorTo(s: ObfuscateRunConfiguration) {}
    override fun resetEditorFrom(s: ObfuscateRunConfiguration) {}
}
