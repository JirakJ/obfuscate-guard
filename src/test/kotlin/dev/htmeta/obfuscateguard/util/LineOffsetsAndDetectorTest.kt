package dev.htmeta.obfuscateguard.util

import dev.htmeta.obfuscateguard.core.ProjectDetector
import dev.htmeta.obfuscateguard.core.model.ProjectType
import dev.htmeta.obfuscateguard.filetype.ProGuardConfigLinter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LineOffsetsAndDetectorTest {

    @Test
    fun `first non-blank offset per line skips spaces and tabs`() {
        val text = "a\n  b\n\t\tc\n\nend"
        val offsets = LineOffsets(text)
        assertEquals(5, offsets.lineCount)
        assertEquals(0, offsets.firstNonBlank(0))
        assertEquals(text.indexOf('b'), offsets.firstNonBlank(1))
        assertEquals(text.indexOf('c'), offsets.firstNonBlank(2))
        assertEquals(text.indexOf("\nend"), offsets.firstNonBlank(3))
        assertEquals(-1, offsets.firstNonBlank(9))
        assertEquals(-1, LineOffsets("").firstNonBlank(0))
    }

    @Test
    fun `gradle project type detection`() {
        assertEquals(ProjectType.ANDROID_APP, ProjectDetector.gradleProjectType("""id("com.android.application")"""))
        assertEquals(ProjectType.ANDROID_LIBRARY, ProjectDetector.gradleProjectType("""id("com.android.library")"""))
        assertEquals(ProjectType.PLUGIN, ProjectDetector.gradleProjectType("""id("org.jetbrains.intellij.platform")"""))
        assertEquals(ProjectType.APP, ProjectDetector.gradleProjectType("application { mainClass.set(\"x\") }"))
        assertEquals(ProjectType.LIBRARY, ProjectDetector.gradleProjectType("plugins { `java-library` }"))
        assertEquals(ProjectType.APP, ProjectDetector.gradleProjectType(""))
    }

    @Test
    fun `maven project type detection`() {
        assertEquals(ProjectType.LIBRARY, ProjectDetector.mavenProjectType("<packaging>pom</packaging>"))
        assertEquals(ProjectType.APP, ProjectDetector.mavenProjectType("<packaging>war</packaging>"))
        assertEquals(ProjectType.APP, ProjectDetector.mavenProjectType("<packaging>jar</packaging>"))
    }

    @Test
    fun `linter offsets stay correct with CRLF line endings`() {
        val content = "-keep class a.B\r\n-dontobfusctae\r\n"
        val issue = ProGuardConfigLinter.lint(content).single()
        assertEquals(2, issue.lineNumber)
        assertEquals("-dontobfusctae", content.substring(issue.startOffset, issue.endOffset))
    }
}
