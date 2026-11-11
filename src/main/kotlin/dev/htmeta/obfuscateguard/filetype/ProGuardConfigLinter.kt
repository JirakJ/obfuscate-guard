package dev.htmeta.obfuscateguard.filetype

/**
 * Pure linting logic for ProGuard configuration content.
 * Used by [ProGuardLintAnnotator] in the editor; fully unit-testable.
 */
object ProGuardConfigLinter {

    enum class IssueSeverity { ERROR, WARNING, WEAK_WARNING }

    data class LintIssue(
        /** Offset of the issue start in the whole file content. */
        val startOffset: Int,
        val endOffset: Int,
        val lineNumber: Int,
        val severity: IssueSeverity,
        val message: String,
    )

    private val DIRECTIVE_AT_LINE_START = Regex("""^(-[A-Za-z]+)""")

    fun lint(content: String): List<LintIssue> {
        val issues = mutableListOf<LintIssue>()
        val seenSingletonDirectives = mutableMapOf<String, Int>()
        var offset = 0
        // Split on '\n' only so offsets stay correct for CRLF files ('\r' stays part of the line).
        content.split('\n').forEachIndexed { index, line ->
            lintLine(line, offset, index + 1, seenSingletonDirectives)?.let { issues += it }
            offset += line.length + 1
        }
        return issues
    }

    private fun lintLine(line: String, lineOffset: Int, lineNumber: Int, seen: MutableMap<String, Int>): LintIssue? {
        val name = DIRECTIVE_AT_LINE_START.find(line)?.groupValues?.get(1) ?: return null
        val start = lineOffset
        val end = start + name.length
        val directive = ProGuardDirectives.find(name)
        return when {
            directive == null -> {
                val hint = ProGuardDirectives.closestMatch(name)?.let { " Did you mean '$it'?" }.orEmpty()
                LintIssue(start, end, lineNumber, IssueSeverity.ERROR, "Unknown directive '$name'.$hint")
            }
            directive.dangerous -> {
                // A targeted -dontwarn for a specific package is normal practice; only broad ones are flagged.
                val argument = line.removePrefix(name).trim()
                val targetedDontwarn = name == "-dontwarn" && argument.isNotEmpty() && argument != "**" && argument != "*"
                if (targetedDontwarn) {
                    null
                } else {
                    LintIssue(start, end, lineNumber, IssueSeverity.WARNING, "${directive.dangerReason} (${directive.name})")
                }
            }
            name in SINGLETON_DIRECTIVES -> {
                val firstLine = seen.putIfAbsent(name, lineNumber)
                firstLine?.let {
                    val message = "Duplicate directive '$name' (already specified on line $it)."
                    LintIssue(start, end, lineNumber, IssueSeverity.WEAK_WARNING, message)
                }
            }
            else -> null
        }
    }

    /** Checks useful for release readiness, not tied to a specific line. */
    fun releaseChecks(content: String): List<String> {
        val findings = mutableListOf<String>()
        val directives = content.lines()
            .map { it.trim() }
            .filter { it.startsWith("-") }

        if (directives.none { it.startsWith("-printmapping") }) {
            findings.add("No -printmapping: without a mapping file you cannot deobfuscate production crashes.")
        }
        if (directives.any { it == "-dontobfuscate" }) {
            findings.add("-dontobfuscate is present: obfuscation is disabled entirely.")
        }
        if (directives.none { it.startsWith("-keepattributes") && it.contains("LineNumberTable") }) {
            findings.add("LineNumberTable attribute is not kept: stack traces will have no line numbers.")
        }
        return findings
    }

    private val SINGLETON_DIRECTIVES = setOf(
        "-printmapping", "-applymapping", "-repackageclasses", "-flattenpackagehierarchy",
        "-optimizationpasses", "-target", "-printseeds", "-printusage", "-printconfiguration",
    )
}
