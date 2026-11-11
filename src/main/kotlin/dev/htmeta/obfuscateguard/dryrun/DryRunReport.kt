package dev.htmeta.obfuscateguard.dryrun

import dev.htmeta.obfuscateguard.core.model.KeepRule
import dev.htmeta.obfuscateguard.core.model.ObfuscationConfig
import dev.htmeta.obfuscateguard.core.model.RiskItem
import dev.htmeta.obfuscateguard.core.model.RiskSeverity
import dev.htmeta.obfuscateguard.policy.PolicyViolation

/** Outcome of a dry run. */
enum class DryRunOutcome { PASS, WARN, FAIL }

data class DryRunResult(val outcome: DryRunOutcome, val verdict: String, val report: String)

/** Pure dry-run evaluation: which risks are not covered by keep rules and what the verdict is. */
object DryRunReport {
    /** More uncovered warnings than this turn the verdict into WARN. */
    const val WARNING_TOLERANCE = 3
    private const val TOP_RISKS = 10

    /** Risks whose class is not mentioned by any accepted keep rule. */
    fun uncoveredRisks(risks: List<RiskItem>, keepRules: List<KeepRule>): List<RiskItem> {
        val patterns = keepRules.filter { it.accepted }.map { it.pattern }
        return risks.filter { risk -> patterns.none { it.contains(risk.className) } }
    }

    /** Problems with the generated engine configuration (`null` = could not be generated). */
    fun configIssues(configContent: String?): List<String> = when {
        configContent == null -> listOf("Cannot generate engine configuration")
        configContent.isBlank() -> listOf("Generated config is empty", "No keep rules in generated config")
        !configContent.contains("-keep") -> listOf("No keep rules in generated config")
        else -> emptyList()
    }

    /**
     * Builds the report. [policyViolations] is `null` when the project has no team policy.
     */
    fun build(
        config: ObfuscationConfig,
        risks: List<RiskItem>,
        configContent: String?,
        policyViolations: List<PolicyViolation>?,
    ): DryRunResult {
        val uncovered = uncoveredRisks(risks, config.keepRules)
        val issues = configIssues(configContent)
        val critical = uncovered.count { it.severity == RiskSeverity.CRITICAL }
        val warnings = uncovered.count { it.severity == RiskSeverity.WARNING }
        val violations = policyViolations.orEmpty()

        val (outcome, verdict) = when {
            violations.isNotEmpty() -> DryRunOutcome.FAIL to "FAIL - ${violations.size} team policy violation(s)"
            critical > 0 -> DryRunOutcome.FAIL to "FAIL - $critical critical risks without keep rules"
            warnings > WARNING_TOLERANCE -> DryRunOutcome.WARN to "WARN - $warnings uncovered warnings"
            issues.isNotEmpty() -> DryRunOutcome.WARN to "WARN - Configuration issues detected"
            else -> DryRunOutcome.PASS to "PASS - All risks covered. Safe to run obfuscation."
        }

        val sb = StringBuilder()
        sb.appendLine("Dry-Run Validation Report")
        sb.appendLine("Engine: ${config.engine.displayName} | Level: ${config.level.displayName} | Keep rules: ${config.keepRules.size}")
        sb.appendLine()
        sb.appendLine(
            "Risks: ${risks.size} total, ${risks.size - uncovered.size} covered, ${uncovered.size} uncovered " +
                "($critical critical, $warnings warnings)"
        )
        if (issues.isNotEmpty()) sb.appendLine("Config issues: ${issues.joinToString("; ")}")
        if (policyViolations != null) {
            sb.appendLine()
            sb.appendLine(if (violations.isEmpty()) "Team policy: compliant" else "Team policy: ${violations.size} violation(s)")
            violations.forEach { sb.appendLine("  ✖ [${it.rule}] ${it.message}") }
        }
        if (uncovered.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Top uncovered risks:")
            uncovered.take(TOP_RISKS).forEach {
                sb.appendLine("  ${it.severity.icon} ${it.className}:${it.lineNumber} - ${it.description}")
            }
        }
        sb.appendLine()
        sb.appendLine("Verdict: $verdict")
        return DryRunResult(outcome, verdict, sb.toString())
    }
}
