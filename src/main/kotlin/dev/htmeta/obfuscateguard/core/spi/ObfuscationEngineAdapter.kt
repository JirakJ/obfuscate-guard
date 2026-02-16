package dev.htmeta.obfuscateguard.core.spi

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import dev.htmeta.obfuscateguard.core.model.*

/**
 * SPI for obfuscation engine adapters.
 * Each engine (ProGuard, yGuard, Allatori) implements this interface.
 * Registered via `dev.htmeta.obfuscateguard.engineAdapter` extension point.
 */
interface ObfuscationEngineAdapter {

    val engineType: EngineType
    val displayName: String

    /** Detect whether the engine is available/configured in the given project. */
    fun detectInstallation(project: Project): EngineInstallation?

    /** Generate engine-specific configuration from the unified config model. */
    fun generateConfig(profile: ProjectProfile, config: ObfuscationConfig): EngineSpecificConfig

    /** Parse a mapping file produced by this engine. */
    fun parseMapping(mappingContent: String): List<MappingEntry>

    /** Build the command line or Gradle/Maven task to run obfuscation. */
    fun buildRunCommand(project: Project, config: EngineSpecificConfig): List<String>

    companion object {
        val EP_NAME = ExtensionPointName.create<ObfuscationEngineAdapter>(
            "dev.htmeta.obfuscateguard.engineAdapter"
        )

        fun findAdapter(engineType: EngineType): ObfuscationEngineAdapter? =
            EP_NAME.extensionList.find { it.engineType == engineType }

        fun allAdapters(): List<ObfuscationEngineAdapter> = EP_NAME.extensionList
    }
}

data class EngineInstallation(
    val engineType: EngineType,
    val version: String? = null,
    val configFilePath: String? = null,
    val isConfigured: Boolean = false,
)

data class MappingEntry(
    val originalName: String,
    val obfuscatedName: String,
    val type: MappingEntryType = MappingEntryType.CLASS,
)

enum class MappingEntryType { CLASS, METHOD, FIELD }
