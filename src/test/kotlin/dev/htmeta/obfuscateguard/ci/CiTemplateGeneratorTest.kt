/*
 * Copyright 2024-2025 Jakub Jirák
 * Licensed under the Apache License, Version 2.0 (see LICENSE).
 */
package dev.htmeta.obfuscateguard.ci

import dev.htmeta.obfuscateguard.core.model.EngineSpecificConfig
import dev.htmeta.obfuscateguard.core.model.EngineType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CiTemplateGeneratorTest {

    private val proguardConfig = EngineSpecificConfig(
        engineType = EngineType.PROGUARD,
        configContent = "-keep class com.example.** { *; }",
        configFilePath = "proguard-rules.pro"
    )

    private val yguardConfig = EngineSpecificConfig(
        engineType = EngineType.YGUARD,
        configContent = "<yguard/>",
        configFilePath = "yguard.xml"
    )

    private val allatoriConfig = EngineSpecificConfig(
        engineType = EngineType.ALLATORI,
        configContent = "<config/>",
        configFilePath = "allatori.xml"
    )

    @Test
    fun `generateGitHubActions contains required workflow structure`() {
        val yaml = CiTemplateGenerator.generateGitHubActions(proguardConfig)
        assertTrue(yaml.contains("name: Obfuscation Build"))
        assertTrue(yaml.contains("runs-on: ubuntu-latest"))
        assertTrue(yaml.contains("actions/checkout@v4"))
        assertTrue(yaml.contains("setup-java@v4"))
        assertTrue(yaml.contains("upload-artifact@v4"))
    }

    @Test
    fun `generateGitHubActions uses correct gradle command for ProGuard`() {
        val yaml = CiTemplateGenerator.generateGitHubActions(proguardConfig)
        assertTrue(yaml.contains("assembleRelease"))
        // The android.enableR8 flag is obsolete (removed in AGP 7) and must not be emitted
        assertFalse(yaml.contains("enableR8"))
    }

    @Test
    fun `generateGitHubActions uses correct gradle command for yGuard`() {
        val yaml = CiTemplateGenerator.generateGitHubActions(yguardConfig)
        assertTrue(yaml.contains("yguardObfuscate"))
    }

    @Test
    fun `generateGitHubActions uses correct gradle command for Allatori`() {
        val yaml = CiTemplateGenerator.generateGitHubActions(allatoriConfig)
        assertTrue(yaml.contains("allatoriObfuscate"))
    }

    @Test
    fun `generateGitLabCI contains required structure`() {
        val yaml = CiTemplateGenerator.generateGitLabCI(proguardConfig)
        assertTrue(yaml.contains("stages:"))
        assertTrue(yaml.contains("stage: obfuscate"))
        assertTrue(yaml.contains("eclipse-temurin:17-jdk"))
        assertTrue(yaml.contains("expire_in: 30 days"))
        assertTrue(yaml.contains("assembleRelease"))
    }

    @Test
    fun `generateJenkinsfile contains required structure`() {
        val jenkinsfile = CiTemplateGenerator.generateJenkinsfile(proguardConfig)
        assertTrue(jenkinsfile.contains("pipeline {"))
        assertTrue(jenkinsfile.contains("agent any"))
        assertTrue(jenkinsfile.contains("tools {"))
        assertTrue(jenkinsfile.contains("JDK17"))
        assertTrue(jenkinsfile.contains("archiveArtifacts"))
        assertTrue(jenkinsfile.contains("assembleRelease"))
    }

    @Test
    fun `generateJenkinsfile uses yGuard command`() {
        val jenkinsfile = CiTemplateGenerator.generateJenkinsfile(yguardConfig)
        assertTrue(jenkinsfile.contains("yguardObfuscate"))
    }

    @Test
    fun `allTemplates returns three templates`() {
        val templates = CiTemplateGenerator.allTemplates(proguardConfig)
        assertEquals(3, templates.size)
        assertTrue(templates.keys.any { it.contains("GitHub Actions") })
        assertTrue(templates.keys.any { it.contains("GitLab CI") })
        assertTrue(templates.keys.any { it.contains("Jenkinsfile") })
    }

    @Test
    fun `allTemplates values are non-empty`() {
        val templates = CiTemplateGenerator.allTemplates(proguardConfig)
        templates.values.forEach { content ->
            assertTrue(content.isNotBlank())
        }
    }

    @Test
    fun `generateGitLabCI uses Allatori command`() {
        val yaml = CiTemplateGenerator.generateGitLabCI(allatoriConfig)
        assertTrue(yaml.contains("allatoriObfuscate"))
    }
}
