package dev.htmeta.obfuscateguard.ui.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.*

class ObfuscateGuardConfigurable : BoundConfigurable("ObfuscateGuard") {

    private val settings get() = ObfuscateGuardSettings.getInstance()

    override fun createPanel() = panel {
        group("General") {
            row {
                checkBox("Enable ObfuscateGuard")
                    .bindSelected(settings::enabled)
            }
            row {
                checkBox("Auto-detect project configuration")
                    .bindSelected(settings::autoDetectProject)
                    .comment("Automatically detect build system, project type, and existing obfuscation config on project open")
            }
            row {
                checkBox("Auto-analyze on project open")
                    .bindSelected(settings::autoAnalyzeOnOpen)
                    .comment("Automatically scan for obfuscation risks when a project is opened")
            }
        }

        group("Defaults") {
            row("Default engine:") {
                comboBox(listOf("ProGuard/R8", "yGuard", "Allatori"))
                    .bindItem(
                        { engineToDisplay(settings.defaultEngine) },
                        { settings.defaultEngine = displayToEngine(it ?: "ProGuard/R8") },
                    )
            }
            row("Default level:") {
                comboBox(listOf("Light", "Standard", "Aggressive"))
                    .bindItem(
                        { settings.defaultLevel.lowercase().replaceFirstChar { it.uppercase() } },
                        { settings.defaultLevel = (it ?: "Standard").uppercase() },
                    )
                    .comment("Light = rename packages only; Standard = rename classes/methods/fields; Aggressive = full + optimization")
            }
        }

        group("Display") {
            row {
                checkBox("Show gutter icons for risk items")
                    .bindSelected(settings::showGutterIcons)
                    .comment("Display warning icons in the editor gutter for lines with obfuscation risks")
            }
        }

        group("Mapping Management") {
            row("Max stored mappings:") {
                spinner(5..500, 5)
                    .bindIntValue(settings::mappingRetentionCount)
                    .comment("Maximum number of mapping files to retain per project")
            }
        }
    }

    private fun engineToDisplay(engine: String): String = when (engine) {
        "PROGUARD" -> "ProGuard/R8"
        "YGUARD" -> "yGuard"
        "ALLATORI" -> "Allatori"
        else -> "ProGuard/R8"
    }

    private fun displayToEngine(display: String): String = when (display) {
        "ProGuard/R8" -> "PROGUARD"
        "yGuard" -> "YGUARD"
        "Allatori" -> "ALLATORI"
        else -> "PROGUARD"
    }
}
