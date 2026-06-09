package dev.htmeta.obfuscateguard.keeprules

import dev.htmeta.obfuscateguard.core.model.KeepRule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KeepRulesExporterTest {

    private val rules = listOf(
        KeepRule(pattern = "class com.example.Api { *; }", reason = "Reflection: Class.forName()"),
        KeepRule(pattern = "class com.example.Dto { *; }", reason = "Serialization"),
    )

    @Test
    fun `renderBlock contains markers, reasons and rules`() {
        val block = KeepRulesExporter.renderBlock(rules)
        assertTrue(block.startsWith(KeepRulesExporter.BEGIN_MARKER))
        assertTrue(block.endsWith(KeepRulesExporter.END_MARKER))
        assertTrue(block.contains("# Reflection: Class.forName()"))
        assertTrue(block.contains("-keep class com.example.Api { *; }"))
        assertTrue(block.contains("-keep class com.example.Dto { *; }"))
    }

    @Test
    fun `renderBlock skips rejected rules and deduplicates patterns`() {
        val block = KeepRulesExporter.renderBlock(
            rules + listOf(
                KeepRule(pattern = "class com.example.Api { *; }", reason = "duplicate"),
                KeepRule(pattern = "class com.example.No { *; }", accepted = false),
            )
        )
        assertEquals(1, Regex.escape("com.example.Api").toRegex().findAll(block).count())
        assertFalse(block.contains("com.example.No"))
    }

    @Test
    fun `merge appends block to empty content`() {
        val merged = KeepRulesExporter.merge("", rules)
        assertTrue(merged.startsWith(KeepRulesExporter.BEGIN_MARKER))
        assertTrue(merged.trimEnd().endsWith(KeepRulesExporter.END_MARKER))
    }

    @Test
    fun `merge preserves user content and appends block`() {
        val existing = "# my rules\n-keep class my.Stuff { *; }\n"
        val merged = KeepRulesExporter.merge(existing, rules)
        assertTrue(merged.startsWith("# my rules"))
        assertTrue(merged.contains("-keep class my.Stuff { *; }"))
        assertTrue(merged.contains(KeepRulesExporter.BEGIN_MARKER))
    }

    @Test
    fun `merge replaces an existing generated block in place`() {
        val firstExport = KeepRulesExporter.merge("# user header\n", rules)
        val withSuffix = firstExport + "\n# user footer\n"
        val newRules = listOf(KeepRule(pattern = "class com.example.Replaced { *; }"))
        val merged = KeepRulesExporter.merge(withSuffix, newRules)

        assertTrue(merged.startsWith("# user header"))
        assertTrue(merged.contains("# user footer"))
        assertTrue(merged.contains("com.example.Replaced"))
        assertFalse(merged.contains("com.example.Api"), "Old generated rules must be replaced")
        assertEquals(1, Regex.escape(KeepRulesExporter.BEGIN_MARKER).toRegex().findAll(merged).count())
    }

    @Test
    fun `repeated merge is idempotent`() {
        val once = KeepRulesExporter.merge("-keep class my.Stuff { *; }", rules)
        val twice = KeepRulesExporter.merge(once, rules)
        assertEquals(once, twice)
    }
}
