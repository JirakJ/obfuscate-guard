package dev.htmeta.obfuscateguard.core.model

import kotlinx.serialization.Serializable

/**
 * Detected project profile with build system, type, and module information.
 */
@Serializable
data class ProjectProfile(
    val projectType: ProjectType = ProjectType.UNKNOWN,
    val buildSystem: BuildSystem = BuildSystem.UNKNOWN,
    val modules: List<ModuleInfo> = emptyList(),
    val existingConfig: ObfuscationConfig? = null,
    val detectedEngines: List<EngineType> = emptyList(),
    val projectName: String = "",
    val basePath: String = "",
)

@Serializable
data class ModuleInfo(
    val name: String,
    val path: String,
    val type: ProjectType = ProjectType.UNKNOWN,
    val hasExistingConfig: Boolean = false,
)

/**
 * Complete obfuscation configuration for a project.
 */
@Serializable
data class ObfuscationConfig(
    val engine: EngineType = EngineType.PROGUARD,
    val level: ObfuscationLevel = ObfuscationLevel.STANDARD,
    val goals: Set<ProtectionGoal> = setOf(ProtectionGoal.IP_PROTECTION),
    val keepRules: List<KeepRule> = emptyList(),
    val version: Int = 1,
)

/**
 * A keep rule that prevents obfuscation of a specific class, method, or field.
 */
@Serializable
data class KeepRule(
    val pattern: String,
    val reason: String = "",
    val source: RiskCategory? = null,
    val needsReview: Boolean = false,
    val accepted: Boolean = true,
) {
    fun toProGuardRule(): String = "-keep $pattern"
}

/**
 * A detected risk that may cause problems after obfuscation.
 */
data class RiskItem(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val className: String,
    val memberName: String? = null,
    val category: RiskCategory,
    val severity: RiskSeverity,
    val description: String,
    val explanation: String = "",
    val suggestedKeepRule: KeepRule? = null,
)

/**
 * Immutable record of a mapping file produced by obfuscation.
 */
@Serializable
data class MappingRecord(
    val id: String,
    val timestamp: Long,
    val gitCommitHash: String? = null,
    val buildVariant: String? = null,
    val engineType: EngineType,
    val engineVersion: String = "",
    val configHash: String = "",
    val mappingFilePath: String,
    val stats: MappingStats = MappingStats(),
)

@Serializable
data class MappingStats(
    val classCount: Int = 0,
    val methodCount: Int = 0,
    val fieldCount: Int = 0,
)

/**
 * Result of an obfuscation run.
 */
data class ObfuscationRunResult(
    val success: Boolean,
    val exitCode: Int = 0,
    val outputPath: String? = null,
    val mappingPath: String? = null,
    val log: String = "",
    val durationMs: Long = 0,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
)

/**
 * Engine-specific configuration (generated from ObfuscationConfig).
 */
data class EngineSpecificConfig(
    val engineType: EngineType,
    val configContent: String,
    val configFilePath: String? = null,
    val additionalFiles: Map<String, String> = emptyMap(),
)
