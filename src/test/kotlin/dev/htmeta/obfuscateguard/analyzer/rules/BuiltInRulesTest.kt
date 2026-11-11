package dev.htmeta.obfuscateguard.analyzer.rules

import dev.htmeta.obfuscateguard.core.model.RiskCategory
import dev.htmeta.obfuscateguard.core.model.RiskSeverity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuiltInRulesTest {

    private val source = """
        package com.example.app

        class Loader {
            fun load() {
                val c = Class.forName("com.example.Plugin")
                val m = c.getMethod("run")
                m.invoke(null)
            }
        }
    """.trimIndent()

    @Test
    fun `reflection patterns report line, class and severity`() {
        val risks = analyzeWithPatterns("/src/Loader.kt", "Loader", source, ReflectionUsageRule().patterns, RiskCategory.REFLECTION)
        val forName = risks.single { it.description.startsWith("Class.forName") }
        assertEquals(5, forName.lineNumber)
        assertEquals("com.example.app.Loader", forName.className)
        assertEquals(RiskSeverity.CRITICAL, forName.severity)
        assertEquals("/src/Loader.kt", forName.filePath)
        assertTrue(risks.any { it.description.startsWith("getMethod") && it.severity == RiskSeverity.WARNING })
    }

    @Test
    fun `suggested keep rules depend on severity`() {
        val risks = analyzeWithPatterns("/x", "X", source, ReflectionUsageRule().patterns, RiskCategory.REFLECTION)
        val critical = risks.first { it.severity == RiskSeverity.CRITICAL }.suggestedKeepRule!!
        assertEquals("class com.example.app.Loader { *; }", critical.pattern)
        assertFalse(critical.needsReview)
        val warning = risks.first { it.severity == RiskSeverity.WARNING }.suggestedKeepRule!!
        assertEquals("class com.example.app.Loader { <methods>; }", warning.pattern)
        assertTrue(warning.needsReview)
        assertEquals(RiskCategory.REFLECTION, warning.source)
    }

    @Test
    fun `fallback name is used when no class is declared`() {
        val risks = analyzeWithPatterns("/s.kts", "script", "Class.forName(x)", ReflectionUsageRule().patterns, RiskCategory.REFLECTION)
        assertEquals("script", risks.single().className)
    }

    @Test
    fun `every built-in rule detects its typical pattern and ignores plain code`() {
        val samples = listOf(
            Triple(SerializationRule().patterns, RiskCategory.SERIALIZATION, "@SerializedName(\"id\") val id: Int"),
            Triple(JniRule().patterns, JniRule().category, "external fun nativeInit()"),
            Triple(JniRule().patterns, JniRule().category, "private native void init();"),
            Triple(DiFrameworkRule().patterns, DiFrameworkRule().category, "@Inject lateinit var repo: Repo"),
            Triple(DynamicLoadingRule().patterns, DynamicLoadingRule().category, "ServiceLoader.load(Plugin::class.java)"),
        )
        samples.forEach { (patterns, category, code) ->
            val hits = analyzeWithPatterns("/f", "F", code, patterns, category)
            assertTrue(hits.isNotEmpty(), "$category should flag: $code")
            assertTrue(hits.all { it.category == category })
            assertTrue(analyzeWithPatterns("/f", "F", "val x = 1", patterns, category).isEmpty())
        }
    }
}
