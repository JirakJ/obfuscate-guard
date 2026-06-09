package dev.htmeta.obfuscateguard.policy

import dev.htmeta.obfuscateguard.core.model.ObfuscationConfig
import dev.htmeta.obfuscateguard.core.model.ObfuscationLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Team-wide obfuscation policy, loaded from `obfuscateguard-policy.json`
 * in the project root (committed to VCS and shared across the team).
 *
 * Example:
 * ```json
 * {
 *   "minLevel": "STANDARD",
 *   "requireMappingOutput": true,
 *   "requiredKeepAttributes": ["SourceFile", "LineNumberTable"],
 *   "forbiddenDirectives": ["-dontobfuscate", "-ignorewarnings"]
 * }
 * ```
 */
@Serializable
data class ObfuscationPolicy(
    val minLevel: ObfuscationLevel = ObfuscationLevel.LIGHT,
    val requireMappingOutput: Boolean = false,
    val requiredKeepAttributes: List<String> = emptyList(),
    val forbiddenDirectives: List<String> = emptyList(),
    val maxUncoveredCriticalRisks: Int = 0,
)

data class PolicyViolation(
    val rule: String,
    val message: String,
)

object PolicyEngine {

    const val POLICY_FILE_NAME = "obfuscateguard-policy.json"

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    fun loadPolicy(projectBasePath: String): ObfuscationPolicy? {
        val file = File(projectBasePath, POLICY_FILE_NAME)
        if (!file.isFile) return null
        return try {
            json.decodeFromString<ObfuscationPolicy>(file.readText())
        } catch (_: Exception) {
            null
        }
    }

    fun savePolicy(projectBasePath: String, policy: ObfuscationPolicy) {
        File(projectBasePath, POLICY_FILE_NAME).writeText(json.encodeToString(ObfuscationPolicy.serializer(), policy))
    }

    /**
     * Validates the chosen config and the generated engine config content
     * against the policy. Returns an empty list when compliant.
     */
    fun validate(
        policy: ObfuscationPolicy,
        config: ObfuscationConfig,
        generatedConfigContent: String,
        uncoveredCriticalRisks: Int = 0,
    ): List<PolicyViolation> {
        val violations = mutableListOf<PolicyViolation>()

        if (config.level.ordinal < policy.minLevel.ordinal) {
            violations.add(
                PolicyViolation(
                    rule = "minLevel",
                    message = "Obfuscation level ${config.level.displayName} is below the required minimum ${policy.minLevel.displayName}.",
                )
            )
        }

        val directives = generatedConfigContent.lines().map { it.trim() }

        if (policy.requireMappingOutput && directives.none { it.startsWith("-printmapping") }) {
            violations.add(
                PolicyViolation(
                    rule = "requireMappingOutput",
                    message = "Policy requires -printmapping so production crashes can be deobfuscated.",
                )
            )
        }

        for (attribute in policy.requiredKeepAttributes) {
            val kept = directives.any { it.startsWith("-keepattributes") && it.contains(attribute) }
            if (!kept) {
                violations.add(
                    PolicyViolation(
                        rule = "requiredKeepAttributes",
                        message = "Policy requires keeping the '$attribute' attribute (-keepattributes $attribute).",
                    )
                )
            }
        }

        for (forbidden in policy.forbiddenDirectives) {
            if (directives.any { it == forbidden || it.startsWith("$forbidden ") }) {
                violations.add(
                    PolicyViolation(
                        rule = "forbiddenDirectives",
                        message = "Directive '$forbidden' is forbidden by team policy.",
                    )
                )
            }
        }

        if (uncoveredCriticalRisks > policy.maxUncoveredCriticalRisks) {
            violations.add(
                PolicyViolation(
                    rule = "maxUncoveredCriticalRisks",
                    message = "$uncoveredCriticalRisks critical risks without keep rules exceed the allowed ${policy.maxUncoveredCriticalRisks}.",
                )
            )
        }

        return violations
    }
}
