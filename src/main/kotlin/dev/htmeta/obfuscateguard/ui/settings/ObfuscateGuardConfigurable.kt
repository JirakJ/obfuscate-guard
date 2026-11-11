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
        }

        group("Defaults") {
            row("Default level:") {
                comboBox(listOf("Light", "Standard", "Aggressive"))
                    .bindItem(
                        { settings.defaultLevel.lowercase().replaceFirstChar { it.uppercase() } },
                        { settings.defaultLevel = (it ?: "Standard").uppercase() },
                    )
                    .comment("Pre-selected in the wizard. Light = shrink/optimize only; Standard = rename; Aggressive = + repackage")
            }
        }

        group("Display") {
            row {
                checkBox("Show gutter icons for risk items")
                    .bindSelected(settings::showGutterIcons)
                    .comment("Display warning icons in the editor gutter for lines with obfuscation risks")
            }
        }
    }
}
