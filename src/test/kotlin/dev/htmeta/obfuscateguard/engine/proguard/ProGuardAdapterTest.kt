/*
 * Copyright (c) 2024-2025 Jakub Jirak. All rights reserved.
 * Licensed under the terms of the project license.
 */
package dev.htmeta.obfuscateguard.engine.proguard

import dev.htmeta.obfuscateguard.core.model.*
import dev.htmeta.obfuscateguard.core.spi.MappingEntryType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ProGuardAdapterTest {

    private val adapter = ProGuardAdapter()

    // --- parseMapping tests ---

    @Test
    fun `parseMapping parses class entries`() {
        val mapping = "com.example.MyClass -> a.b.c:\n"
        val entries = adapter.parseMapping(mapping)
        assertEquals(1, entries.size)
        assertEquals("com.example.MyClass", entries[0].originalName)
        assertEquals("a.b.c", entries[0].obfuscatedName)
        assertEquals(MappingEntryType.CLASS, entries[0].type)
    }

    @Test
    fun `parseMapping parses method entries`() {
        val mapping = """
            com.example.MyClass -> a.b.c:
                void doStuff(int) -> a
        """.trimIndent()
        val entries = adapter.parseMapping(mapping)
        assertEquals(2, entries.size)
        val methodEntry = entries.find { it.type == MappingEntryType.METHOD }!!
        assertTrue(methodEntry.originalName.contains("doStuff"))
        assertTrue(methodEntry.obfuscatedName.contains("a"))
    }

    @Test
    fun `parseMapping parses field entries`() {
        val mapping = """
            com.example.MyClass -> a.b.c:
                int myField -> d
        """.trimIndent()
        val entries = adapter.parseMapping(mapping)
        val fieldEntry = entries.find { it.type == MappingEntryType.FIELD }!!
        assertTrue(fieldEntry.originalName.contains("myField"))
    }

    @Test
    fun `parseMapping skips comments and blank lines`() {
        val mapping = """
            # ProGuard mapping file
            
            com.example.MyClass -> a.b.c:
        """.trimIndent()
        val entries = adapter.parseMapping(mapping)
        assertEquals(1, entries.size)
    }

    @Test
    fun `parseMapping handles multiple classes`() {
        val mapping = """
            com.example.Foo -> a.a:
                void bar() -> a
            com.example.Baz -> a.b:
                int qux -> b
        """.trimIndent()
        val entries = adapter.parseMapping(mapping)
        assertEquals(4, entries.size)
        val classes = entries.filter { it.type == MappingEntryType.CLASS }
        assertEquals(2, classes.size)
    }

    @Test
    fun `parseMapping returns empty list for empty input`() {
        val entries = adapter.parseMapping("")
        assertTrue(entries.isEmpty())
    }

    // --- generateConfig tests ---

    @Test
    fun `generateConfig for LIGHT level includes keepnames`() {
        val profile = ProjectProfile(projectType = ProjectType.APP)
        val config = ObfuscationConfig(level = ObfuscationLevel.LIGHT)
        val result = adapter.generateConfig(profile, config)
        assertTrue(result.configContent.contains("-keepnames"))
        assertTrue(result.configContent.contains("-keeppackagenames"))
        assertEquals(EngineType.PROGUARD, result.engineType)
    }

    @Test
    fun `generateConfig for STANDARD level includes optimizationpasses 1`() {
        val profile = ProjectProfile(projectType = ProjectType.APP)
        val config = ObfuscationConfig(level = ObfuscationLevel.STANDARD)
        val result = adapter.generateConfig(profile, config)
        assertTrue(result.configContent.contains("-optimizationpasses 1"))
    }

    @Test
    fun `generateConfig for AGGRESSIVE level includes aggressive options`() {
        val profile = ProjectProfile(projectType = ProjectType.APP)
        val config = ObfuscationConfig(level = ObfuscationLevel.AGGRESSIVE)
        val result = adapter.generateConfig(profile, config)
        assertTrue(result.configContent.contains("-optimizationpasses 3"))
        assertTrue(result.configContent.contains("-allowaccessmodification"))
        assertTrue(result.configContent.contains("-repackageclasses"))
        assertTrue(result.configContent.contains("-overloadaggressively"))
    }

    @Test
    fun `generateConfig for ANDROID_APP includes Android keep rules`() {
        val profile = ProjectProfile(projectType = ProjectType.ANDROID_APP)
        val config = ObfuscationConfig()
        val result = adapter.generateConfig(profile, config)
        assertTrue(result.configContent.contains("android.app.Activity"))
        assertTrue(result.configContent.contains("android.app.Service"))
        assertTrue(result.configContent.contains("BroadcastReceiver"))
        assertTrue(result.configContent.contains("Parcelable"))
    }

    @Test
    fun `generateConfig for LIBRARY keeps public API`() {
        val profile = ProjectProfile(projectType = ProjectType.LIBRARY)
        val config = ObfuscationConfig()
        val result = adapter.generateConfig(profile, config)
        assertTrue(result.configContent.contains("public protected"))
    }

    @Test
    fun `generateConfig for APP keeps main method`() {
        val profile = ProjectProfile(projectType = ProjectType.APP)
        val config = ObfuscationConfig()
        val result = adapter.generateConfig(profile, config)
        assertTrue(result.configContent.contains("public static void main"))
    }

    @Test
    fun `generateConfig includes custom keep rules`() {
        val profile = ProjectProfile(projectType = ProjectType.APP)
        val config = ObfuscationConfig(
            keepRules = listOf(
                KeepRule(pattern = "class com.example.KeepMe { *; }", reason = "Reflection", accepted = true),
                KeepRule(pattern = "class com.example.SkipMe { *; }", accepted = false)
            )
        )
        val result = adapter.generateConfig(profile, config)
        assertTrue(result.configContent.contains("-keep class com.example.KeepMe"))
        assertFalse(result.configContent.contains("SkipMe"))
    }

    @Test
    fun `generateConfig includes safe defaults`() {
        val profile = ProjectProfile(projectType = ProjectType.APP)
        val config = ObfuscationConfig()
        val result = adapter.generateConfig(profile, config)
        assertTrue(result.configContent.contains("SerializedName"))
        assertTrue(result.configContent.contains("JsonProperty"))
        assertTrue(result.configContent.contains("Serializable"))
        assertTrue(result.configContent.contains("-dontwarn kotlin.**"))
    }

    @Test
    fun `generateConfig includes STRING_PROTECTION comment when goal is set`() {
        val profile = ProjectProfile(projectType = ProjectType.APP)
        val config = ObfuscationConfig(goals = setOf(ProtectionGoal.STRING_PROTECTION))
        val result = adapter.generateConfig(profile, config)
        assertTrue(result.configContent.contains("String protection"))
    }

    @Test
    fun `adapter has correct engine type and display name`() {
        assertEquals(EngineType.PROGUARD, adapter.engineType)
        assertEquals("ProGuard/R8", adapter.displayName)
    }
}
