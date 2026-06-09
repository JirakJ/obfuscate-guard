package dev.htmeta.obfuscateguard.policy

import dev.htmeta.obfuscateguard.core.model.ObfuscationConfig
import dev.htmeta.obfuscateguard.core.model.ObfuscationLevel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class PolicyEngineTest {

    private val strictPolicy = ObfuscationPolicy(
        minLevel = ObfuscationLevel.STANDARD,
        requireMappingOutput = true,
        requiredKeepAttributes = listOf("SourceFile", "LineNumberTable"),
        forbiddenDirectives = listOf("-dontobfuscate", "-ignorewarnings"),
        maxUncoveredCriticalRisks = 0,
    )

    private val compliantConfigContent = """
        -printmapping mapping.txt
        -keepattributes SourceFile,LineNumberTable
        -keep class com.example.Main { *; }
    """.trimIndent()

    @Test
    fun `compliant config passes validation`() {
        val violations = PolicyEngine.validate(
            strictPolicy,
            ObfuscationConfig(level = ObfuscationLevel.STANDARD),
            compliantConfigContent,
            uncoveredCriticalRisks = 0,
        )
        assertTrue(violations.isEmpty(), "Unexpected: $violations")
    }

    @Test
    fun `level below minimum is a violation`() {
        val violations = PolicyEngine.validate(
            strictPolicy,
            ObfuscationConfig(level = ObfuscationLevel.LIGHT),
            compliantConfigContent,
        )
        assertTrue(violations.any { it.rule == "minLevel" })
    }

    @Test
    fun `missing printmapping is a violation when required`() {
        val violations = PolicyEngine.validate(
            strictPolicy,
            ObfuscationConfig(level = ObfuscationLevel.STANDARD),
            "-keepattributes SourceFile,LineNumberTable",
        )
        assertTrue(violations.any { it.rule == "requireMappingOutput" })
    }

    @Test
    fun `missing required keep attribute is a violation`() {
        val violations = PolicyEngine.validate(
            strictPolicy,
            ObfuscationConfig(level = ObfuscationLevel.STANDARD),
            "-printmapping m.txt\n-keepattributes SourceFile",
        )
        assertEquals(1, violations.count { it.rule == "requiredKeepAttributes" })
        assertTrue(violations.first { it.rule == "requiredKeepAttributes" }.message.contains("LineNumberTable"))
    }

    @Test
    fun `forbidden directive is a violation`() {
        val violations = PolicyEngine.validate(
            strictPolicy,
            ObfuscationConfig(level = ObfuscationLevel.STANDARD),
            compliantConfigContent + "\n-dontobfuscate",
        )
        assertTrue(violations.any { it.rule == "forbiddenDirectives" && it.message.contains("-dontobfuscate") })
    }

    @Test
    fun `uncovered critical risks above limit is a violation`() {
        val violations = PolicyEngine.validate(
            strictPolicy,
            ObfuscationConfig(level = ObfuscationLevel.STANDARD),
            compliantConfigContent,
            uncoveredCriticalRisks = 2,
        )
        assertTrue(violations.any { it.rule == "maxUncoveredCriticalRisks" })
    }

    @Test
    fun `default policy permits a light unconfigured setup`() {
        val violations = PolicyEngine.validate(
            ObfuscationPolicy(),
            ObfuscationConfig(level = ObfuscationLevel.LIGHT),
            "-keep class A { *; }",
        )
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `save and load round-trips the policy`(@TempDir tempDir: Path) {
        PolicyEngine.savePolicy(tempDir.toString(), strictPolicy)
        val loaded = PolicyEngine.loadPolicy(tempDir.toString())
        assertEquals(strictPolicy, loaded)
    }

    @Test
    fun `loadPolicy returns null when file is missing or invalid`(@TempDir tempDir: Path) {
        assertNull(PolicyEngine.loadPolicy(tempDir.toString()))
        File(tempDir.toFile(), PolicyEngine.POLICY_FILE_NAME).writeText("{ not json")
        assertNull(PolicyEngine.loadPolicy(tempDir.toString()))
    }

    @Test
    fun `loadPolicy ignores unknown keys for forward compatibility`(@TempDir tempDir: Path) {
        File(tempDir.toFile(), PolicyEngine.POLICY_FILE_NAME)
            .writeText("""{"minLevel":"AGGRESSIVE","futureOption":true}""")
        val loaded = PolicyEngine.loadPolicy(tempDir.toString())
        assertNotNull(loaded)
        assertEquals(ObfuscationLevel.AGGRESSIVE, loaded!!.minLevel)
    }
}
