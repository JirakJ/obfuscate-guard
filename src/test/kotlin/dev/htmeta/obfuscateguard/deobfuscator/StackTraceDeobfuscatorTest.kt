package dev.htmeta.obfuscateguard.deobfuscator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StackTraceDeobfuscatorTest {

    private val mapping = """
        com.example.app.MainService -> a.a.a:
            void start() -> a
            12:34:void handleRequest(java.lang.String) -> b
            int requestCount -> c
        com.example.app.util.Helper -> a.a.b:
            7:9:java.lang.String format(int) -> a
        com.example.app.Outer${'$'}Inner -> a.b.c:
            void run() -> a
    """.trimIndent()

    @Test
    fun `deobfuscates class and method names in at-frames`() {
        val trace = "\tat a.a.a.b(SourceFile:42)"
        val result = StackTraceDeobfuscator.deobfuscate(trace, mapping)
        assertEquals("\tat com.example.app.MainService.handleRequest(MainService.java:42)", result)
    }

    @Test
    fun `deobfuscates method without line-range prefix`() {
        val trace = "\tat a.a.a.a(Unknown Source)"
        val result = StackTraceDeobfuscator.deobfuscate(trace, mapping)
        assertEquals("\tat com.example.app.MainService.start(MainService.java)", result)
    }

    @Test
    fun `deobfuscates class names in exception message line`() {
        val trace = "java.lang.NullPointerException: cannot invoke a.a.b method"
        val result = StackTraceDeobfuscator.deobfuscate(trace, mapping)
        assertEquals("java.lang.NullPointerException: cannot invoke com.example.app.util.Helper method", result)
    }

    @Test
    fun `does not corrupt unrelated identifiers when replacing class names`() {
        // "a.a.ab" must NOT be partially replaced via the "a.a.a" or "a.a.b" mapping
        val trace = "message a.a.ab stays"
        val result = StackTraceDeobfuscator.deobfuscate(trace, mapping)
        assertEquals("message a.a.ab stays", result)
    }

    @Test
    fun `deobfuscates a multi-line stack trace end to end`() {
        val trace = """
            java.lang.IllegalStateException: failure in a.a.a
            ${'\t'}at a.a.a.b(SourceFile:42)
            ${'\t'}at a.a.b.a(SourceFile:8)
            ${'\t'}at a.b.c.a(SourceFile:3)
        """.trimIndent()
        val result = StackTraceDeobfuscator.deobfuscate(trace, mapping)
        val lines = result.lines()
        assertEquals("java.lang.IllegalStateException: failure in com.example.app.MainService", lines[0])
        assertEquals("\tat com.example.app.MainService.handleRequest(MainService.java:42)", lines[1])
        assertEquals("\tat com.example.app.util.Helper.format(Helper.java:8)", lines[2])
        assertTrue(lines[3].contains("com.example.app.Outer\$Inner.run"))
    }

    @Test
    fun `inner class source file drops the dollar suffix`() {
        val trace = "\tat a.b.c.a(SourceFile:3)"
        val result = StackTraceDeobfuscator.deobfuscate(trace, mapping)
        assertEquals("\tat com.example.app.Outer\$Inner.run(Outer.java:3)", result)
    }

    @Test
    fun `unmapped frames are left untouched`() {
        val trace = "\tat java.base/java.lang.Thread.run(Thread.java:833)"
        val result = StackTraceDeobfuscator.deobfuscate(trace, mapping)
        assertTrue(result.contains("java.lang.Thread"))
    }

    @Test
    fun `returns message for mapping without entries`() {
        val result = StackTraceDeobfuscator.deobfuscate("at a.a(X:1)", "# just a comment")
        assertEquals("No mapping entries found in the file", result)
    }

    @Test
    fun `preserves trailing text after the frame`() {
        val trace = "\tat a.a.a.b(SourceFile:42) ~[app.jar:?]"
        val result = StackTraceDeobfuscator.deobfuscate(trace, mapping)
        assertEquals("\tat com.example.app.MainService.handleRequest(MainService.java:42) ~[app.jar:?]", result)
    }
}
