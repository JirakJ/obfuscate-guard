package dev.htmeta.obfuscateguard.dryrun

import dev.htmeta.obfuscateguard.core.model.KeepRule
import dev.htmeta.obfuscateguard.core.model.ObfuscationConfig
import dev.htmeta.obfuscateguard.core.model.RiskCategory
import dev.htmeta.obfuscateguard.core.model.RiskItem
import dev.htmeta.obfuscateguard.core.model.RiskSeverity
import dev.htmeta.obfuscateguard.policy.PolicyViolation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DryRunReportTest {

    private fun risk(cls: String, severity: RiskSeverity) = RiskItem(
        id = cls + severity, filePath = "/$cls.kt", lineNumber = 3, className = cls,
        category = RiskCategory.REFLECTION, severity = severity, description = "desc $cls",
    )

    private val validConfig = "-keep class com.A { *; }"

    @Test
    fun `risks named by an accepted keep rule are covered`() {
        val rules = listOf(KeepRule("class com.A { *; }"), KeepRule("class com.B { *; }", accepted = false))
        val risks = listOf(risk("com.A", RiskSeverity.CRITICAL), risk("com.B", RiskSeverity.CRITICAL))
        val uncovered = DryRunReport.uncoveredRisks(risks, rules)
        assertEquals(listOf("com.B"), uncovered.map { it.className })
    }

    @Test
    fun `config issues`() {
        assertEquals(listOf("Cannot generate engine configuration"), DryRunReport.configIssues(null))
        assertEquals(2, DryRunReport.configIssues(" ").size)
        assertEquals(listOf("No keep rules in generated config"), DryRunReport.configIssues("-dontwarn x"))
        assertTrue(DryRunReport.configIssues(validConfig).isEmpty())
    }

    @Test
    fun `pass when everything is covered and config is valid`() {
        val result = DryRunReport.build(ObfuscationConfig(), emptyList(), validConfig, null)
        assertEquals(DryRunOutcome.PASS, result.outcome)
        assertTrue(result.report.contains("Risks: 0 total"))
        assertFalse(result.report.contains("Team policy"))
    }

    @Test
    fun `uncovered critical risk fails and is listed`() {
        val result = DryRunReport.build(ObfuscationConfig(), listOf(risk("com.X", RiskSeverity.CRITICAL)), validConfig, emptyList())
        assertEquals(DryRunOutcome.FAIL, result.outcome)
        assertEquals("FAIL - 1 critical risks without keep rules", result.verdict)
        assertTrue(result.report.contains("Team policy: compliant"))
        assertTrue(result.report.contains("com.X:3 - desc com.X"))
    }

    @Test
    fun `policy violations take precedence`() {
        val result = DryRunReport.build(ObfuscationConfig(), emptyList(), validConfig, listOf(PolicyViolation("minLevel", "too weak")))
        assertEquals(DryRunOutcome.FAIL, result.outcome)
        assertTrue(result.report.contains("✖ [minLevel] too weak"))
    }

    @Test
    fun `warnings above tolerance or config issues warn`() {
        val warnings = (1..DryRunReport.WARNING_TOLERANCE + 1).map { risk("com.W$it", RiskSeverity.WARNING) }
        assertEquals(DryRunOutcome.WARN, DryRunReport.build(ObfuscationConfig(), warnings, validConfig, null).outcome)
        val few = warnings.take(DryRunReport.WARNING_TOLERANCE)
        assertEquals(DryRunOutcome.PASS, DryRunReport.build(ObfuscationConfig(), few, validConfig, null).outcome)
        assertEquals(DryRunOutcome.WARN, DryRunReport.build(ObfuscationConfig(), emptyList(), null, null).outcome)
    }
}
