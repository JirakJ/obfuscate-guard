package dev.htmeta.obfuscateguard.filetype

import dev.htmeta.obfuscateguard.filetype.ProGuardConfigLinter.IssueSeverity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ProGuardConfigLinterTest {

    @Test
    fun `clean config produces no issues`() {
        val content = """
            # Production config
            -keepattributes SourceFile,LineNumberTable
            -printmapping mapping.txt
            -keep class com.example.Main { *; }
        """.trimIndent()
        assertTrue(ProGuardConfigLinter.lint(content).isEmpty())
    }

    @Test
    fun `unknown directive is an error with typo suggestion`() {
        val issues = ProGuardConfigLinter.lint("-keepatributes SourceFile")
        assertEquals(1, issues.size)
        assertEquals(IssueSeverity.ERROR, issues[0].severity)
        assertTrue(issues[0].message.contains("-keepattributes"), "Expected suggestion in: ${issues[0].message}")
    }

    @Test
    fun `issue offsets point at the directive`() {
        val content = "# comment\n-dontobfuscate\n"
        val issues = ProGuardConfigLinter.lint(content)
        assertEquals(1, issues.size)
        val issue = issues[0]
        assertEquals("-dontobfuscate", content.substring(issue.startOffset, issue.endOffset))
        assertEquals(2, issue.lineNumber)
    }

    @Test
    fun `dontobfuscate and ignorewarnings are flagged as dangerous`() {
        val issues = ProGuardConfigLinter.lint("-dontobfuscate\n-ignorewarnings")
        assertEquals(2, issues.size)
        assertTrue(issues.all { it.severity == IssueSeverity.WARNING })
    }

    @Test
    fun `broad dontwarn is flagged but targeted dontwarn is fine`() {
        assertEquals(1, ProGuardConfigLinter.lint("-dontwarn **").size)
        assertEquals(1, ProGuardConfigLinter.lint("-dontwarn").size)
        assertTrue(ProGuardConfigLinter.lint("-dontwarn okhttp3.**").isEmpty())
    }

    @Test
    fun `duplicate singleton directive is a weak warning`() {
        val content = """
            -printmapping out1.txt
            -keep class A { *; }
            -printmapping out2.txt
        """.trimIndent()
        val issues = ProGuardConfigLinter.lint(content)
        assertEquals(1, issues.size)
        assertEquals(IssueSeverity.WEAK_WARNING, issues[0].severity)
        assertTrue(issues[0].message.contains("line 1"))
        assertEquals(3, issues[0].lineNumber)
    }

    @Test
    fun `comments and member lines are not linted as directives`() {
        val content = """
            # -notadirective in a comment
            -keep class com.example.Foo {
                native <methods>;
            }
        """.trimIndent()
        assertTrue(ProGuardConfigLinter.lint(content).isEmpty())
    }

    @Test
    fun `release checks flag missing printmapping`() {
        val findings = ProGuardConfigLinter.releaseChecks("-keep class A { *; }")
        assertTrue(findings.any { it.contains("-printmapping") })
    }

    @Test
    fun `release checks flag dontobfuscate and missing LineNumberTable`() {
        val findings = ProGuardConfigLinter.releaseChecks("-dontobfuscate\n-printmapping m.txt")
        assertTrue(findings.any { it.contains("-dontobfuscate") })
        assertTrue(findings.any { it.contains("LineNumberTable") })
    }

    @Test
    fun `release checks pass for a production-ready config`() {
        val content = """
            -printmapping mapping.txt
            -keepattributes SourceFile,LineNumberTable
        """.trimIndent()
        assertTrue(ProGuardConfigLinter.releaseChecks(content).isEmpty())
    }
}
