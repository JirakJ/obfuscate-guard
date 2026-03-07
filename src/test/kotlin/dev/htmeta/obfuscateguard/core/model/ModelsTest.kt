/*
 * Copyright (c) 2024-2025 Jakub Jirak. All rights reserved.
 * Licensed under the terms of the project license.
 */
package dev.htmeta.obfuscateguard.core.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModelsTest {

    @Test
    fun `KeepRule toProGuardRule formats correctly`() {
        val rule = KeepRule(pattern = "class com.example.Foo { *; }", reason = "Reflection")
        assertEquals("-keep class com.example.Foo { *; }", rule.toProGuardRule())
    }

    @Test
    fun `KeepRule defaults are correct`() {
        val rule = KeepRule(pattern = "class A { *; }")
        assertEquals("", rule.reason)
        assertNull(rule.source)
        assertFalse(rule.needsReview)
        assertTrue(rule.accepted)
    }

    @Test
    fun `ProjectProfile defaults to unknown type and build system`() {
        val profile = ProjectProfile()
        assertEquals(ProjectType.UNKNOWN, profile.projectType)
        assertEquals(BuildSystem.UNKNOWN, profile.buildSystem)
        assertTrue(profile.modules.isEmpty())
        assertNull(profile.existingConfig)
        assertTrue(profile.detectedEngines.isEmpty())
        assertEquals("", profile.projectName)
        assertEquals("", profile.basePath)
    }

    @Test
    fun `ObfuscationConfig defaults are correct`() {
        val config = ObfuscationConfig()
        assertEquals(EngineType.PROGUARD, config.engine)
        assertEquals(ObfuscationLevel.STANDARD, config.level)
        assertEquals(setOf(ProtectionGoal.IP_PROTECTION), config.goals)
        assertTrue(config.keepRules.isEmpty())
        assertEquals(1, config.version)
    }

    @Test
    fun `ModuleInfo stores all fields`() {
        val module = ModuleInfo(
            name = ":app",
            path = "app",
            type = ProjectType.ANDROID_APP,
            hasExistingConfig = true
        )
        assertEquals(":app", module.name)
        assertEquals("app", module.path)
        assertEquals(ProjectType.ANDROID_APP, module.type)
        assertTrue(module.hasExistingConfig)
    }

    @Test
    fun `RiskItem stores all fields`() {
        val keepRule = KeepRule(pattern = "class Foo { *; }", reason = "Reflection usage")
        val item = RiskItem(
            id = "r-1",
            filePath = "/src/Foo.kt",
            lineNumber = 42,
            className = "com.example.Foo",
            memberName = "doStuff",
            category = RiskCategory.REFLECTION,
            severity = RiskSeverity.CRITICAL,
            description = "Class.forName() usage",
            explanation = "Will break after obfuscation",
            suggestedKeepRule = keepRule
        )
        assertEquals("r-1", item.id)
        assertEquals(42, item.lineNumber)
        assertEquals(RiskCategory.REFLECTION, item.category)
        assertEquals(RiskSeverity.CRITICAL, item.severity)
        assertNotNull(item.suggestedKeepRule)
    }

    @Test
    fun `MappingRecord stores all fields with defaults`() {
        val record = MappingRecord(
            id = "m-1",
            timestamp = 1000L,
            engineType = EngineType.PROGUARD,
            mappingFilePath = "/build/mapping.txt"
        )
        assertEquals("m-1", record.id)
        assertEquals(1000L, record.timestamp)
        assertNull(record.gitCommitHash)
        assertNull(record.buildVariant)
        assertEquals("", record.engineVersion)
        assertEquals(0, record.stats.classCount)
    }

    @Test
    fun `MappingStats defaults to zero counts`() {
        val stats = MappingStats()
        assertEquals(0, stats.classCount)
        assertEquals(0, stats.methodCount)
        assertEquals(0, stats.fieldCount)
    }

    @Test
    fun `ObfuscationRunResult stores success state`() {
        val result = ObfuscationRunResult(success = true, exitCode = 0, durationMs = 5000)
        assertTrue(result.success)
        assertEquals(0, result.exitCode)
        assertEquals(5000, result.durationMs)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `ObfuscationRunResult stores failure state`() {
        val result = ObfuscationRunResult(
            success = false, exitCode = 1,
            errors = listOf("Missing config", "Invalid rule"),
            log = "Build failed"
        )
        assertFalse(result.success)
        assertEquals(2, result.errors.size)
    }

    @Test
    fun `EngineSpecificConfig stores all fields`() {
        val config = EngineSpecificConfig(
            engineType = EngineType.YGUARD,
            configContent = "<yguard/>",
            configFilePath = "yguard.xml",
            additionalFiles = mapOf("extra.xml" to "<extra/>")
        )
        assertEquals(EngineType.YGUARD, config.engineType)
        assertEquals("<yguard/>", config.configContent)
        assertEquals(1, config.additionalFiles.size)
    }
}
