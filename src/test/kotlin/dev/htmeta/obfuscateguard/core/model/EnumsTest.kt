/*
 * Copyright 2024-2025 Jakub Jirák
 * Licensed under the Apache License, Version 2.0 (see LICENSE).
 */
package dev.htmeta.obfuscateguard.core.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EnumsTest {

    @Test
    fun `EngineType has three values with correct display names`() {
        assertEquals(3, EngineType.entries.size)
        assertEquals("ProGuard/R8", EngineType.PROGUARD.displayName)
        assertEquals("yGuard", EngineType.YGUARD.displayName)
        assertEquals("Allatori", EngineType.ALLATORI.displayName)
    }

    @Test
    fun `ObfuscationLevel has three levels with descriptions`() {
        assertEquals(3, ObfuscationLevel.entries.size)
        assertEquals("Light", ObfuscationLevel.LIGHT.displayName)
        assertTrue(ObfuscationLevel.LIGHT.description.contains("minimal"))
        assertEquals("Standard", ObfuscationLevel.STANDARD.displayName)
        assertTrue(ObfuscationLevel.STANDARD.description.contains("balance"))
        assertEquals("Aggressive", ObfuscationLevel.AGGRESSIVE.displayName)
        assertTrue(ObfuscationLevel.AGGRESSIVE.description.contains("maximum"))
    }

    @Test
    fun `ProtectionGoal has three goals`() {
        assertEquals(3, ProtectionGoal.entries.size)
        assertEquals("Intellectual Property Protection", ProtectionGoal.IP_PROTECTION.displayName)
        assertEquals("Anti-Reverse Engineering", ProtectionGoal.ANTI_REVERSE_ENGINEERING.displayName)
        assertEquals("String Encryption", ProtectionGoal.STRING_PROTECTION.displayName)
    }

    @Test
    fun `ProjectType has six types`() {
        assertEquals(6, ProjectType.entries.size)
        assertEquals("Application", ProjectType.APP.displayName)
        assertEquals("Library", ProjectType.LIBRARY.displayName)
        assertEquals("Android App", ProjectType.ANDROID_APP.displayName)
        assertEquals("Android Library", ProjectType.ANDROID_LIBRARY.displayName)
        assertEquals("IDE Plugin", ProjectType.PLUGIN.displayName)
        assertEquals("Unknown", ProjectType.UNKNOWN.displayName)
    }

    @Test
    fun `BuildSystem has three values`() {
        assertEquals(3, BuildSystem.entries.size)
        assertEquals("Gradle", BuildSystem.GRADLE.displayName)
        assertEquals("Maven", BuildSystem.MAVEN.displayName)
        assertEquals("Unknown", BuildSystem.UNKNOWN.displayName)
    }

    @Test
    fun `RiskCategory has six categories`() {
        assertEquals(6, RiskCategory.entries.size)
        assertEquals("Reflection", RiskCategory.REFLECTION.displayName)
        assertEquals("Serialization", RiskCategory.SERIALIZATION.displayName)
        assertEquals("JNI / Native", RiskCategory.JNI.displayName)
        assertEquals("Dependency Injection", RiskCategory.DI_FRAMEWORK.displayName)
        assertEquals("Dynamic Loading", RiskCategory.DYNAMIC_LOADING.displayName)
        assertEquals("Android-specific", RiskCategory.ANDROID.displayName)
    }

    @Test
    fun `RiskSeverity has three levels with icons`() {
        assertEquals(3, RiskSeverity.entries.size)
        assertEquals("Critical", RiskSeverity.CRITICAL.displayName)
        assertEquals("🔴", RiskSeverity.CRITICAL.icon)
        assertEquals("Warning", RiskSeverity.WARNING.displayName)
        assertEquals("🟡", RiskSeverity.WARNING.icon)
        assertEquals("Info", RiskSeverity.INFO.displayName)
        assertEquals("🔵", RiskSeverity.INFO.icon)
    }

    @Test
    fun `EngineType valueOf works correctly`() {
        assertEquals(EngineType.PROGUARD, EngineType.valueOf("PROGUARD"))
        assertEquals(EngineType.YGUARD, EngineType.valueOf("YGUARD"))
        assertEquals(EngineType.ALLATORI, EngineType.valueOf("ALLATORI"))
    }

    @Test
    fun `ObfuscationLevel ordering is preserved`() {
        val levels = ObfuscationLevel.entries
        assertEquals(ObfuscationLevel.LIGHT, levels[0])
        assertEquals(ObfuscationLevel.STANDARD, levels[1])
        assertEquals(ObfuscationLevel.AGGRESSIVE, levels[2])
    }
}
