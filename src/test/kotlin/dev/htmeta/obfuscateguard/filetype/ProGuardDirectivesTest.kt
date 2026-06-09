package dev.htmeta.obfuscateguard.filetype

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ProGuardDirectivesTest {

    @Test
    fun `registry contains the essential directives`() {
        val essential = listOf(
            "-keep", "-keepclassmembers", "-keepclasseswithmembers", "-keepnames",
            "-keepattributes", "-dontwarn", "-dontobfuscate", "-dontshrink", "-dontoptimize",
            "-printmapping", "-applymapping", "-repackageclasses", "-optimizationpasses",
            "-injars", "-outjars", "-libraryjars", "-if", "-assumenosideeffects",
        )
        for (name in essential) {
            assertTrue(ProGuardDirectives.isKnown(name), "Missing directive: $name")
        }
    }

    @Test
    fun `registry has at least 50 directives`() {
        assertTrue(ProGuardDirectives.ALL.size >= 50, "Only ${ProGuardDirectives.ALL.size} directives")
    }

    @Test
    fun `all directives start with a dash and have a description`() {
        for (directive in ProGuardDirectives.ALL) {
            assertTrue(directive.name.startsWith("-"), "${directive.name} must start with '-'")
            assertTrue(directive.description.isNotBlank(), "${directive.name} has no description")
        }
    }

    @Test
    fun `directive names are unique`() {
        val names = ProGuardDirectives.ALL.map { it.name }
        assertEquals(names.size, names.distinct().size)
    }

    @Test
    fun `dangerous directives carry a reason`() {
        val dangerous = ProGuardDirectives.ALL.filter { it.dangerous }
        assertTrue(dangerous.isNotEmpty())
        for (directive in dangerous) {
            assertTrue(directive.dangerReason.isNotBlank(), "${directive.name} dangerous without reason")
        }
    }

    @Test
    fun `find returns directive by exact name`() {
        val keep = ProGuardDirectives.find("-keep")
        assertNotNull(keep)
        assertEquals("-keep", keep!!.name)
        assertNull(ProGuardDirectives.find("-nonexistent"))
    }

    @Test
    fun `closestMatch suggests a fix for a typo`() {
        assertEquals("-keepattributes", ProGuardDirectives.closestMatch("-keepatributes"))
        assertEquals("-dontwarn", ProGuardDirectives.closestMatch("-dontwran"))
        assertEquals("-printmapping", ProGuardDirectives.closestMatch("-printmaping"))
    }

    @Test
    fun `closestMatch returns null for gibberish`() {
        assertNull(ProGuardDirectives.closestMatch("-xyzqwphol"))
    }

    @Test
    fun `levenshtein distance is correct`() {
        assertEquals(0, ProGuardDirectives.levenshtein("abc", "abc"))
        assertEquals(1, ProGuardDirectives.levenshtein("abc", "abd"))
        assertEquals(1, ProGuardDirectives.levenshtein("abc", "abcd"))
        assertEquals(3, ProGuardDirectives.levenshtein("", "abc"))
        assertEquals(3, ProGuardDirectives.levenshtein("kitten", "sitting"))
    }
}
